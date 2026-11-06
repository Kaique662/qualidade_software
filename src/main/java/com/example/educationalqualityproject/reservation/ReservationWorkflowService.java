package com.example.educationalqualityproject.reservation;

import com.example.educationalqualityproject.entity.ManagedResource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Service
@ConditionalOnProperty(prefix = "reservations.datasource", name = "url")
public class ReservationWorkflowService {

    private final ReservationStore repository;
    private final ReservationService reservationService;
    private final ReservationResourceValidator resourcePlanningService;

    public ReservationWorkflowService(ReservationStore repository,
                                      ReservationService reservationService,
                                      ReservationResourceValidator resourcePlanningService) {
        this.repository = repository;
        this.reservationService = reservationService;
        this.resourcePlanningService = resourcePlanningService;
    }

    public ReservationJdbcRepository.ReservationData request(ReservationRequest request) {
        validateRequest(request);
        try {
            int quantity = request.quantity() == null ? 1 : request.quantity();
            boolean restricted = false;
            int materialCapacity = 0;
            List<ReservationStore.MaterialInterval> scheduleMaterialReservations = List.of();
            int preparationBuffer = 0;
            int cleanupBuffer = 0;
            if (request.resourceType() == ReservationRequest.ResourceType.PROFESSOR) {
                resourcePlanningService.validateTeacherReservation(request.resourceId(), request.academicPeriodId(),
                        localDateTime(request.startsAt()), localDateTime(request.endsAt()));
            } else {
                ManagedResource resource = resourcePlanningService.validateReservationResource(
                        request.resourceId(), localDateTime(request.startsAt()),
                        localDateTime(request.endsAt()), quantity);
                validateResourceType(request.resourceType(), resource);
                restricted = resource.isRestricted();
                preparationBuffer = resource.getPreparationBufferMinutes();
                cleanupBuffer = resource.getCleanupBufferMinutes();
                if (request.resourceType() == ReservationRequest.ResourceType.MATERIAL) {
                    materialCapacity = resource.getTotalQuantity();
                    scheduleMaterialReservations = resourcePlanningService.getMaterialReservations(
                        request.resourceId(), localDateTime(request.startsAt()),
                        localDateTime(request.endsAt()));
                }
            }

            ReservationService.Resource resource = new ReservationService.Resource(
                    request.resourceId(), restricted, List.of());
            ReservationService.Actor requester = new ReservationService.Actor(
                    request.requesterId(), ReservationService.Role.SOLICITANTE);
            Optional<ReservationService.Reservation> reservation = reservationService.request(
                    resource, requester, request.startsAt(), request.endsAt());
            if (reservation.isEmpty()) {
                throw conflict("O recurso está em manutenção no intervalo solicitado");
            }

            Instant bufferedStart = request.startsAt().minusSeconds(preparationBuffer * 60L);
            Instant bufferedEnd = request.endsAt().plusSeconds(cleanupBuffer * 60L);
                return repository.create(request, restricted, quantity, bufferedStart, bufferedEnd,
                    materialCapacity, scheduleMaterialReservations);
        } catch (ResponseStatusException exception) {
            repository.recordRejectedAttempt(request.resourceId(), request.requesterId(),
                    exception.getReason() == null ? "Solicitação rejeitada" : exception.getReason());
            throw exception;
        } catch (DataIntegrityViolationException exception) {
            repository.recordRejectedAttempt(request.resourceId(), request.requesterId(),
                    "Conflito de recurso ou capacidade de material");
            if (hasSqlState(exception, "23P01") || hasSqlState(exception, "23505")) {
                throw conflict("O recurso já está reservado ou a quantidade disponível foi excedida");
            }
            throw exception;
        }
    }

    public ReservationJdbcRepository.ReservationData approve(long id, String actorId,
                                                              ReservationService.Role role) {
        requireText(actorId, "Identificador do responsável é obrigatório");
        if (role == null) {
            throw badRequest("Papel do responsável é obrigatório");
        }
        ReservationJdbcRepository.ReservationData current = find(id);
        if (current.status() != ReservationService.Status.SOLICITADA) {
            recordRejected(current.resourceId(), actorId, "Aprovação em estado inválido");
            throw conflict("Somente reservas solicitadas podem ser aprovadas");
        }
        ReservationService.Reservation domainReservation = toDomain(current);
        reservationService.approve(domainReservation, new ReservationService.Actor(actorId, role));
        if (domainReservation.status() != ReservationService.Status.APROVADA) {
            recordRejected(current.resourceId(), actorId, "Aprovação sem papel permitido ou autoaprovação restrita");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Aprovação exige papel Responsável e, para recurso restrito, solicitante diferente");
        }
        return transition(id, current, ReservationService.Status.SOLICITADA,
                ReservationService.Status.APROVADA, actorId);
    }

    public ReservationJdbcRepository.ReservationData startUse(long id, String actorId) {
        requireText(actorId, "Identificador do responsável é obrigatório");
        ReservationJdbcRepository.ReservationData current = find(id);
        ReservationService.Reservation domainReservation = toDomain(current);
        reservationService.startUse(domainReservation);
        if (domainReservation.status() != ReservationService.Status.EM_USO) {
            recordRejected(current.resourceId(), actorId, "Início de uso em estado inválido");
            throw conflict("Somente reservas aprovadas podem iniciar o uso");
        }
        return transition(id, current, ReservationService.Status.APROVADA,
                ReservationService.Status.EM_USO, actorId);
    }

    public ReservationJdbcRepository.ReservationData complete(long id, String actorId) {
        requireText(actorId, "Identificador do responsável é obrigatório");
        ReservationJdbcRepository.ReservationData current = find(id);
        ReservationService.Reservation domainReservation = toDomain(current);
        reservationService.complete(domainReservation);
        if (domainReservation.status() != ReservationService.Status.CONCLUIDA) {
            recordRejected(current.resourceId(), actorId, "Conclusão em estado inválido");
            throw conflict("Somente reservas em uso podem ser concluídas");
        }
        return transition(id, current, ReservationService.Status.EM_USO,
                ReservationService.Status.CONCLUIDA, actorId);
    }

    public ReservationJdbcRepository.ReservationData find(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva não encontrada"));
    }

    public List<ReservationJdbcRepository.ReservationData> findAll(String resourceId, String status,
                                                                   Instant startsBefore, Instant endsAfter) {
        return repository.findAll(resourceId, status, startsBefore, endsAfter);
    }

    public List<ReservationJdbcRepository.AuditEvent> history(long id) {
        find(id);
        return repository.findAuditEvents(id);
    }

    private ReservationService.Reservation toDomain(ReservationJdbcRepository.ReservationData data) {
        ReservationService.Resource resource = new ReservationService.Resource(
                data.resourceId(), data.restricted(), List.of());
        ReservationService.Actor requester = new ReservationService.Actor(
                data.requesterId(), data.requesterRole());
        return new ReservationService.Reservation(resource, requester, data.status(),
                data.startsAt(), data.endsAt(), data.requestedAt());
    }

    private ReservationJdbcRepository.ReservationData transition(
            long id, ReservationJdbcRepository.ReservationData current,
            ReservationService.Status expected, ReservationService.Status next, String actorId) {
        try {
            return repository.transition(id, expected, next, actorId);
        } catch (ResponseStatusException exception) {
            recordRejected(current.resourceId(), actorId,
                    exception.getReason() == null ? "Transição rejeitada" : exception.getReason());
            throw exception;
        }
    }

    private void recordRejected(String resourceId, String actorId, String details) {
        requireText(actorId, "Identificador do responsável é obrigatório");
        repository.recordRejectedAttempt(resourceId, actorId, details);
    }

    private void validateRequest(ReservationRequest request) {
        if (request == null) {
            throw badRequest("Dados da reserva são obrigatórios");
        }
        requireText(request.resourceId(), "Identificador do recurso é obrigatório");
        requireText(request.requesterId(), "Identificador do solicitante é obrigatório");
        if (request.resourceType() == null) {
            throw badRequest("Tipo do recurso é obrigatório");
        }
        if (request.startsAt() == null || request.endsAt() == null
                || !request.endsAt().isAfter(request.startsAt())) {
            throw badRequest("A reserva deve ter um intervalo válido");
        }
        int quantity = request.quantity() == null ? 1 : request.quantity();
        if (quantity < 1 || (request.resourceType() != ReservationRequest.ResourceType.MATERIAL && quantity != 1)) {
            throw badRequest("A quantidade deve ser positiva; salas e professores usam quantidade 1");
        }
    }

    private void validateResourceType(ReservationRequest.ResourceType requestedType, ManagedResource resource) {
        ManagedResource.ResourceType expected = requestedType == ReservationRequest.ResourceType.ROOM
                ? ManagedResource.ResourceType.ROOM : ManagedResource.ResourceType.MATERIAL;
        if (resource.getType() != expected) {
            throw badRequest("O tipo do recurso não corresponde ao tipo solicitado");
        }
    }

    private boolean hasSqlState(Throwable error, String expected) {
        Throwable current = error;
        while (current != null) {
            if (current instanceof SQLException sqlException && expected.equals(sqlException.getSQLState())) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private LocalDateTime localDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw badRequest(message);
        }
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }
}