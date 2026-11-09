package com.example.educationalqualityproject.reservation;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void responsibleCanApproveUnrestrictedReservation() {
        ReservationService service = new ReservationService(
                Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC));
        ReservationService.Resource resource = new ReservationService.Resource("room-1", false);
        ReservationService.Actor requester = new ReservationService.Actor(
                "user-1", ReservationService.Role.SOLICITANTE);
        ReservationService.Actor responsible = new ReservationService.Actor(
                "user-2", ReservationService.Role.RESPONSAVEL);
        ReservationService.Reservation reservation = service.request(resource, requester);

        service.approve(reservation, responsible);

        assertEquals(ReservationService.Status.APROVADA, reservation.status());
    }

        @Test
        void requesterCannotApproveOwnRestrictedReservation() {
                ReservationService service = new ReservationService(
                                Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC));
                ReservationService.Resource resource = new ReservationService.Resource("room-1", true);
                ReservationService.Actor requesterAndApprover = new ReservationService.Actor(
                                "user-1", ReservationService.Role.RESPONSAVEL);
                ReservationService.Reservation reservation = service.request(resource, requesterAndApprover);

                service.approve(reservation, requesterAndApprover);

                assertEquals(ReservationService.Status.SOLICITADA, reservation.status());
        }

        @Test
        void reservationCrossingMaintenanceStartByOneSecondIsRejected() {
                Clock clock = Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC);
                ReservationService service = new ReservationService(clock);
                Instant maintenanceStart = clock.instant();
                ReservationService.Resource resource = new ReservationService.Resource(
                                "room-1", false,
                                List.of(new ReservationService.MaintenancePeriod(
                                                maintenanceStart, maintenanceStart.plusSeconds(60))));
                ReservationService.Actor requester = new ReservationService.Actor(
                                "user-1", ReservationService.Role.SOLICITANTE);
                Instant reservationStart = maintenanceStart.minusSeconds(10);
                Instant reservationEnd = maintenanceStart.plusSeconds(1);

                Optional<ReservationService.Reservation> result = service.request(
                                resource, requester, reservationStart, reservationEnd);

                assertTrue(result.isEmpty());
        }

        @Test
        void approvedReservationMovesThroughInUseToCompleted() {
                ReservationService service = new ReservationService(
                                Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC));
                ReservationService.Resource resource = new ReservationService.Resource("room-1", true);
                ReservationService.Actor requester = new ReservationService.Actor(
                                "user-1", ReservationService.Role.SOLICITANTE);
                ReservationService.Actor responsible = new ReservationService.Actor(
                                "user-2", ReservationService.Role.RESPONSAVEL);
                ReservationService.Reservation reservation = service.request(resource, requester);

                service.approve(reservation, responsible);
                assertEquals(ReservationService.Status.APROVADA, reservation.status());

                service.startUse(reservation);
                assertEquals(ReservationService.Status.EM_USO, reservation.status());

                service.complete(reservation);
                assertEquals(ReservationService.Status.CONCLUIDA, reservation.status());
        }

        @Test
        void invalidTransitionsDoNotAdvanceReservationState() {
                ReservationService service = new ReservationService(
                                Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC));
                ReservationService.Resource resource = new ReservationService.Resource("room-1", true);
                ReservationService.Actor requester = new ReservationService.Actor(
                                "user-1", ReservationService.Role.SOLICITANTE);
                ReservationService.Actor responsible = new ReservationService.Actor(
                                "user-2", ReservationService.Role.RESPONSAVEL);

                ReservationService.Reservation requested = service.request(resource, requester);
                service.startUse(requested);
                assertEquals(ReservationService.Status.SOLICITADA, requested.status());

                ReservationService.Reservation approved = service.request(resource, requester);
                service.approve(approved, responsible);
                service.complete(approved);
                assertEquals(ReservationService.Status.APROVADA, approved.status());
        }

        @Test
        void approveDoesNotChangeReservationThatIsNoLongerRequested() {
        ReservationService service = new ReservationService(
                Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC));
        ReservationService.Resource resource = new ReservationService.Resource("room-1", true);
        ReservationService.Actor requester = new ReservationService.Actor(
                "user-1", ReservationService.Role.SOLICITANTE);
        ReservationService.Actor responsible = new ReservationService.Actor(
                "user-2", ReservationService.Role.RESPONSAVEL);

        ReservationService.Reservation reservation = service.request(resource, requester);
        service.approve(reservation, responsible);
        service.startUse(reservation);
        assertEquals(ReservationService.Status.EM_USO, reservation.status());

        service.approve(reservation, responsible);

        assertEquals(ReservationService.Status.EM_USO, reservation.status());
    }
}