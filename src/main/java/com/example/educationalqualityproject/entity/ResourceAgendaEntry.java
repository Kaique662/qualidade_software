package com.example.educationalqualityproject.entity;

import java.time.LocalDateTime;

public record ResourceAgendaEntry(
        String allocationId,
        String scheduleId,
        String resourceId,
        String resourceType,
        String courseId,
        String classId,
        String subjectId,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        int quantity) {
}