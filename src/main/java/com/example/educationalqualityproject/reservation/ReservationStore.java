package com.example.educationalqualityproject.reservation;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ReservationStore {

    ReservationJdbcRepository.ReservationData create(ReservationRequest request, boolean restricted, int quantity,
                                                      Instant bufferedStart, Instant bufferedEnd,
                                                      int materialCapacity, List<MaterialInterval> scheduleReservations);

    ReservationJdbcRepository.ReservationData transition(long id, ReservationService.Status expected,
                                                           ReservationService.Status next, String actorId);

    Optional<ReservationJdbcRepository.ReservationData> findById(long id);

    List<ReservationJdbcRepository.ReservationData> findAll(String resourceId, String status,
                                                             Instant startsBefore, Instant endsAfter);

    List<ReservationJdbcRepository.AuditEvent> findAuditEvents(long reservationId);

    List<MaterialInterval> findActiveMaterialReservations(String resourceId, Instant startsAt, Instant endsAt);

    void recordRejectedAttempt(String resourceId, String actorId, String details);

    record MaterialInterval(Instant startsAt, Instant endsAt, int quantity) { }
}