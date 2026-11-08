package com.example.educationalqualityproject.entity;

import java.time.LocalDateTime;
import java.util.List;

public record TeacherScheduleView(
        String scheduleId,
        String name,
        String academicPeriod,
        String status,
        int versionNumber,
        LocalDateTime publishedAt,
        List<Activity> activities) {

    public record Activity(
            String activityId,
            String course,
            String className,
            String subject,
            LocalDateTime startsAt,
            LocalDateTime endsAt,
            String room,
            String location,
            List<String> materials) { }
}