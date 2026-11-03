package com.example.educationalqualityproject.reservation;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class ReservationService {

    private final Clock clock;

    public ReservationService(Clock clock) {
        this.clock = clock;
    }

    public Reservation request(Resource resource, Actor requester) {
        return new Reservation(resource, requester, Status.SOLICITADA,
            null, null, clock.instant());
        }

        public Optional<Reservation> request(Resource resource, Actor requester,
                         Instant startsAt, Instant endsAt) {
            if (overlapsMaintenance(resource, startsAt, endsAt)) {
                return Optional.empty();
            }
        return Optional.of(new Reservation(resource, requester, Status.SOLICITADA,
            startsAt, endsAt, clock.instant()));
    }

            private boolean overlapsMaintenance(Resource resource, Instant startsAt, Instant endsAt) {
            return resource.maintenancePeriods().stream()
                .anyMatch(period -> startsAt.isBefore(period.endsAt())
                    && endsAt.isAfter(period.startsAt()));
            }

    public Reservation approve(Reservation reservation, Actor approver) {
        if (canApproveRestrictedReservationExcludingRequester(reservation, approver)) {
            reservation.setStatus(Status.APROVADA);
        }
        return reservation;
    }

    public Reservation startUse(Reservation reservation) {
        return transition(reservation, Status.APROVADA, Status.EM_USO);
    }

    public Reservation complete(Reservation reservation) {
        return transition(reservation, Status.EM_USO, Status.CONCLUIDA);
    }

    private Reservation transition(Reservation reservation, Status expected, Status next) {
        if (reservation.status() == expected) {
            reservation.setStatus(next);
        }
        return reservation;
    }

        private boolean canApproveRestrictedReservationExcludingRequester(
            Reservation reservation,
            Actor approver) {
        return reservation.resource().restricted()
                && approver.role() == Role.RESPONSAVEL
                && !reservation.requester().id().equals(approver.id());
    }

    public enum Role {
        SOLICITANTE,
        RESPONSAVEL
    }

    public enum Status {
        SOLICITADA,
        APROVADA,
        EM_USO,
        CONCLUIDA,
        REJEITADA,
        CANCELADA,
        NAO_COMPARECEU
    }

    public record Resource(String id, boolean restricted, List<MaintenancePeriod> maintenancePeriods) {
        public Resource(String id, boolean restricted) {
            this(id, restricted, List.of());
        }
    }

    public record MaintenancePeriod(Instant startsAt, Instant endsAt) {
    }

    public record Actor(String id, Role role) {
    }

    public static class Reservation {
        private final Resource resource;
        private final Actor requester;
        private Status status;
        private final Instant startsAt;
        private final Instant endsAt;
        private final Instant requestedAt;

        public Reservation(Resource resource, Actor requester, Status status,
                           Instant startsAt, Instant endsAt, Instant requestedAt) {
            this.resource = resource;
            this.requester = requester;
            this.status = status;
            this.startsAt = startsAt;
            this.endsAt = endsAt;
            this.requestedAt = requestedAt;
        }

        public Resource resource() {
            return resource;
        }

        public Actor requester() {
            return requester;
        }

        public Status status() {
            return status;
        }

        public Instant startsAt() {
            return startsAt;
        }

        public Instant endsAt() {
            return endsAt;
        }

        public Instant requestedAt() {
            return requestedAt;
        }

        public void setStatus(Status status) {
            this.status = status;
        }
    }
}