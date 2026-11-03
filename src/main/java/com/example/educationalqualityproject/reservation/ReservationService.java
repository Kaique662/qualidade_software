package com.example.educationalqualityproject.reservation;

import java.time.Clock;

public class ReservationService {

    private final Clock clock;

    public ReservationService(Clock clock) {
        this.clock = clock;
    }

    public Reservation request(Resource resource, Actor requester) {
        return new Reservation(resource, requester, Status.SOLICITADA);
    }

    public Reservation approve(Reservation reservation, Actor approver) {
        if (canApproveRestrictedReservation(reservation, approver)) {
            reservation.setStatus(Status.APROVADA);
        }
        return reservation;
    }

    private boolean canApproveRestrictedReservation(Reservation reservation, Actor approver) {
        return reservation.resource().restricted() && approver.role() == Role.RESPONSAVEL;
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

    public record Resource(String id, boolean restricted) {
    }

    public record Actor(String id, Role role) {
    }

    public static class Reservation {
        private final Resource resource;
        private final Actor requester;
        private Status status;

        public Reservation(Resource resource, Actor requester, Status status) {
            this.resource = resource;
            this.requester = requester;
            this.status = status;
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

        public void setStatus(Status status) {
            this.status = status;
        }
    }
}