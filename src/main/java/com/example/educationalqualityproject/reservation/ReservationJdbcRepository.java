package com.example.educationalqualityproject.reservation;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;

@Repository
@ConditionalOnProperty(prefix = "reservations.datasource", name = "url")
public class ReservationJdbcRepository implements ReservationStore {

    private static final String ACTIVE_STATUSES = "('SOLICITADA', 'APROVADA', 'EM_USO')";
    private final JdbcTemplate jdbcTemplate;

    public ReservationJdbcRepository(@Qualifier("reservationJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(transactionManager = "reservationTransactionManager")
        public ReservationData create(ReservationRequest request, boolean restricted, int quantity,
                                                                  Instant bufferedStart, Instant bufferedEnd, int materialCapacity,
                                                                  List<ReservationStore.MaterialInterval> scheduleReservations) {
        if (request.resourceType() == ReservationRequest.ResourceType.MATERIAL) {
            lockMaterial(request.resourceId());
                        ensureMaterialCapacity(request.resourceId(), bufferedStart, bufferedEnd, quantity,
                                        materialCapacity, scheduleReservations);
        }

        Long generatedId = jdbcTemplate.queryForObject(
                "INSERT INTO reservation (resource_type, resource_id, requester_id, requester_role, "
                        + "restricted, status, starts_at, ends_at, quantity, requested_at) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id",
                Long.class, request.resourceType().name(), request.resourceId(), request.requesterId(),
                ReservationService.Role.SOLICITANTE.name(), restricted,
                ReservationService.Status.SOLICITADA.name(), Timestamp.from(request.startsAt()),
                Timestamp.from(request.endsAt()), quantity, Timestamp.from(Instant.now()));
        if (generatedId == null) {
            throw new IllegalStateException("O banco não retornou o identificador da reserva");
        }
        long reservationId = generatedId.longValue();
        insertResourceReservation(reservationId, request, quantity, bufferedStart, bufferedEnd);
        insertAuditEvent(reservationId, request.resourceId(), request.requesterId(), "RESERVA_SOLICITADA",
                "SUCCEEDED", null, ReservationService.Status.SOLICITADA.name(), null);
        return findById(reservationId).orElseThrow();
    }

    @Transactional(transactionManager = "reservationTransactionManager")
        public ReservationData transition(long id, ReservationService.Status expected,
                                                                          ReservationService.Status next, String actorId) {
        int updated = jdbcTemplate.update(
                "UPDATE reservation SET status = ?, approved_by = CASE WHEN ? = 'APROVADA' THEN ? ELSE approved_by END, "
                        + "approved_at = CASE WHEN ? = 'APROVADA' THEN CURRENT_TIMESTAMP ELSE approved_at END "
                        + "WHERE id = ? AND status = ?",
                next.name(), next.name(), actorId, next.name(), id, expected.name());
        if (updated != 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A reserva mudou de estado; consulte novamente antes de tentar a transição");
        }
        updateResourceStatus(id, next.name());
        insertAuditEvent(id, resourceId(id), actorId, "RESERVA_" + next.name(),
                "SUCCEEDED", expected.name(), next.name(), null);
        return findById(id).orElseThrow();
    }

    @Transactional(readOnly = true, transactionManager = "reservationTransactionManager")
        public Optional<ReservationData> findById(long id) {
        List<ReservationData> result = jdbcTemplate.query(
                "SELECT id, resource_type, resource_id, requester_id, requester_role, restricted, status, "
                        + "starts_at, ends_at, quantity, requested_at, approved_by, approved_at "
                        + "FROM reservation WHERE id = ?",
                this::mapReservation, id);
        return result.stream().findFirst();
    }

    @Transactional(readOnly = true, transactionManager = "reservationTransactionManager")
        public List<ReservationData> findAll(String resourceId, String status, Instant startsBefore, Instant endsAfter) {
                StringBuilder sql = new StringBuilder(
                "SELECT id, resource_type, resource_id, requester_id, requester_role, restricted, status, "
                                                + "starts_at, ends_at, quantity, requested_at, approved_by, approved_at FROM reservation WHERE 1=1");
                List<Object> parameters = new ArrayList<>();
                if (resourceId != null) {
                        sql.append(" AND resource_id = ?");
                        parameters.add(resourceId);
                }
                if (status != null) {
                        sql.append(" AND status = ?");
                        parameters.add(status);
                }
                if (startsBefore != null) {
                        sql.append(" AND starts_at < ?");
                        parameters.add(timestamp(startsBefore));
                }
                if (endsAfter != null) {
                        sql.append(" AND ends_at > ?");
                        parameters.add(timestamp(endsAfter));
                }
                sql.append(" ORDER BY requested_at DESC");
                return jdbcTemplate.query(sql.toString(), this::mapReservation, parameters.toArray());
    }

    @Transactional(readOnly = true, transactionManager = "reservationTransactionManager")
        public List<AuditEvent> findAuditEvents(long reservationId) {
        return jdbcTemplate.query(
                "SELECT id, reservation_id, resource_id, actor_id, action, outcome, previous_status, "
                        + "new_status, details, occurred_at FROM reservation_audit_event "
                        + "WHERE reservation_id = ? ORDER BY occurred_at, id",
                (result, row) -> new AuditEvent(result.getLong("id"), result.getLong("reservation_id"),
                        result.getString("resource_id"), result.getString("actor_id"), result.getString("action"),
                        result.getString("outcome"), result.getString("previous_status"),
                        result.getString("new_status"), result.getString("details"),
                        result.getTimestamp("occurred_at").toInstant()), reservationId);
    }

    @Transactional(readOnly = true, transactionManager = "reservationTransactionManager")
    public List<ReservationStore.MaterialInterval> findActiveMaterialReservations(
            String resourceId, Instant startsAt, Instant endsAt) {
        return jdbcTemplate.query(
                "SELECT lower(time_range), upper(time_range), quantity FROM material_reservation "
                        + "WHERE resource_id = ? AND status IN " + ACTIVE_STATUSES
                        + " AND time_range && tstzrange(?, ?, '[)')",
                (result, row) -> new ReservationStore.MaterialInterval(result.getTimestamp(1).toInstant(),
                        result.getTimestamp(2).toInstant(), result.getInt(3)),
                resourceId, timestamp(startsAt), timestamp(endsAt));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW,
            transactionManager = "reservationTransactionManager")
        public void recordRejectedAttempt(String resourceId, String actorId, String details) {
        insertAuditEvent(null, resourceId, actorId, "RESERVA_REJEITADA", "REJECTED", null, null, details);
    }

    private void insertResourceReservation(long id, ReservationRequest request, int quantity,
                                           Instant bufferedStart, Instant bufferedEnd) {
        String startsAt = "tstzrange(?, ?, '[)')";
        switch (request.resourceType()) {
            case ROOM -> jdbcTemplate.update(
                    "INSERT INTO room_reservation (reservation_id, resource_id, time_range, status) "
                            + "VALUES (?, ?, " + startsAt + ", ?)",
                    id, request.resourceId(), timestamp(bufferedStart), timestamp(bufferedEnd),
                    ReservationService.Status.SOLICITADA.name());
            case PROFESSOR -> jdbcTemplate.update(
                    "INSERT INTO teacher_agenda (reservation_id, professor_id, time_range, status) "
                            + "VALUES (?, ?, " + startsAt + ", ?)",
                    id, request.resourceId(), timestamp(bufferedStart), timestamp(bufferedEnd),
                    ReservationService.Status.SOLICITADA.name());
            case MATERIAL -> jdbcTemplate.update(
                    "INSERT INTO material_reservation (reservation_id, resource_id, quantity, time_range, status) "
                            + "VALUES (?, ?, ?, " + startsAt + ", ?)",
                    id, request.resourceId(), quantity, timestamp(bufferedStart), timestamp(bufferedEnd),
                    ReservationService.Status.SOLICITADA.name());
        }
    }

    private void updateResourceStatus(long reservationId, String status) {
        jdbcTemplate.update("UPDATE room_reservation SET status = ? WHERE reservation_id = ?", status, reservationId);
        jdbcTemplate.update("UPDATE teacher_agenda SET status = ? WHERE reservation_id = ?", status, reservationId);
        jdbcTemplate.update("UPDATE material_reservation SET status = ? WHERE reservation_id = ?", status, reservationId);
    }

    private void lockMaterial(String resourceId) {
        jdbcTemplate.execute((ConnectionCallback<Void>) connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT pg_advisory_xact_lock(hashtext(?))")) {
                statement.setString(1, resourceId);
                statement.execute();
            }
            return null;
        });
    }

    private void ensureMaterialCapacity(String resourceId, Instant startsAt, Instant endsAt,
                                        int requestedQuantity, int capacity,
                                        List<ReservationStore.MaterialInterval> scheduleReservations) {
        List<ReservationStore.MaterialInterval> intervals = findActiveMaterialReservations(
                resourceId, startsAt, endsAt);
        TreeMap<Instant, Integer> events = new TreeMap<>();
        addEvents(events, startsAt, endsAt, requestedQuantity);
        for (MaterialInterval interval : intervals) {
            addEvents(events, interval.startsAt(), interval.endsAt(), interval.quantity());
        }
        for (ReservationStore.MaterialInterval interval : scheduleReservations) {
            addEvents(events, interval.startsAt(), interval.endsAt(), interval.quantity());
        }
        int current = 0;
        int maximum = 0;
        for (int delta : events.values()) {
            current += delta;
            maximum = Math.max(maximum, current);
        }
        if (maximum > capacity) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Quantidade de material excede o estoque disponível no intervalo");
        }
    }

    private void addEvents(TreeMap<Instant, Integer> events, Instant startsAt, Instant endsAt, int quantity) {
        events.merge(startsAt, quantity, Integer::sum);
        events.merge(endsAt, -quantity, Integer::sum);
    }

    private void insertAuditEvent(Long reservationId, String resourceId, String actorId, String action,
                                  String outcome, String previousStatus, String newStatus, String details) {
        jdbcTemplate.update(
                "INSERT INTO reservation_audit_event (reservation_id, resource_id, actor_id, action, outcome, "
                        + "previous_status, new_status, details) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                reservationId, resourceId, actorId, action, outcome, previousStatus, newStatus, details);
    }

    private String resourceId(long reservationId) {
        return jdbcTemplate.queryForObject("SELECT resource_id FROM reservation WHERE id = ?", String.class,
                reservationId);
    }

    private ReservationData mapReservation(ResultSet result, int row) throws SQLException {
        Timestamp approvedAt = result.getTimestamp("approved_at");
        return new ReservationData(result.getLong("id"),
                ReservationRequest.ResourceType.valueOf(result.getString("resource_type")),
                result.getString("resource_id"), result.getString("requester_id"),
                ReservationService.Role.valueOf(result.getString("requester_role")),
                result.getBoolean("restricted"), ReservationService.Status.valueOf(result.getString("status")),
                result.getTimestamp("starts_at").toInstant(), result.getTimestamp("ends_at").toInstant(),
                result.getInt("quantity"), result.getTimestamp("requested_at").toInstant(),
                result.getString("approved_by"), approvedAt == null ? null : approvedAt.toInstant());
    }

    private Timestamp timestamp(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }

    public record ReservationData(long id, ReservationRequest.ResourceType resourceType, String resourceId,
                                  String requesterId, ReservationService.Role requesterRole, boolean restricted,
                                  ReservationService.Status status, Instant startsAt, Instant endsAt,
                                  int quantity, Instant requestedAt, String approvedBy, Instant approvedAt) { }

    public record AuditEvent(long id, long reservationId, String resourceId, String actorId, String action,
                             String outcome, String previousStatus, String newStatus, String details,
                             Instant occurredAt) { }
}