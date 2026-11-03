package com.example.educationalqualityproject.reservation;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReservationServiceTest {

    @Test
    void restrictedResourceReservationRemainsRequestedUntilResponsibleApproves() {
        ReservationService service = new ReservationService(
                Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC));
        ReservationService.Resource resource = new ReservationService.Resource("room-1", true);
        ReservationService.Actor requester = new ReservationService.Actor(
                "user-1", ReservationService.Role.SOLICITANTE);
        ReservationService.Actor responsible = new ReservationService.Actor(
                "user-2", ReservationService.Role.RESPONSAVEL);

        ReservationService.Reservation reservation = service.request(resource, requester);

        assertEquals(ReservationService.Status.SOLICITADA, reservation.status());
        service.approve(reservation, responsible);
        assertEquals(ReservationService.Status.APROVADA, reservation.status());
    }
}