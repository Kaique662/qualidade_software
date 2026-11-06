package com.example.educationalqualityproject.reservation;

import java.time.Instant;

public record ReservationRequest(
        String resourceId,
        ResourceType resourceType,
        String requesterId,
        String academicPeriodId,
        Instant startsAt,
        Instant endsAt,
        Integer quantity) {

    public enum ResourceType {
        ROOM,
        PROFESSOR,
        MATERIAL
    }
}