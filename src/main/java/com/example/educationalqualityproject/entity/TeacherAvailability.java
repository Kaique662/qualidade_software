package com.example.educationalqualityproject.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "teacher_availability")
public class TeacherAvailability {

    @Id
    private String id;
    private String teacherId;
    private String academicPeriodId;
    private AvailabilityType type;
    private ConstraintKind constraintKind;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private boolean recurringWeekly;
    private LocalDate validFrom;
    private LocalDate validUntil;
    private LocalTime dailyStartsAt;
    private LocalTime dailyEndsAt;
    private List<DayOfWeek> weekdays = new ArrayList<>();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }
    public String getAcademicPeriodId() { return academicPeriodId; }
    public void setAcademicPeriodId(String academicPeriodId) { this.academicPeriodId = academicPeriodId; }
    public AvailabilityType getType() { return type; }
    public void setType(AvailabilityType type) { this.type = type; }
    public ConstraintKind getConstraintKind() { return constraintKind; }
    public void setConstraintKind(ConstraintKind constraintKind) { this.constraintKind = constraintKind; }
    public LocalDateTime getStartsAt() { return startsAt; }
    public void setStartsAt(LocalDateTime startsAt) { this.startsAt = startsAt; }
    public LocalDateTime getEndsAt() { return endsAt; }
    public void setEndsAt(LocalDateTime endsAt) { this.endsAt = endsAt; }
    public boolean isRecurringWeekly() { return recurringWeekly; }
    public void setRecurringWeekly(boolean recurringWeekly) { this.recurringWeekly = recurringWeekly; }
    public LocalDate getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDate validFrom) { this.validFrom = validFrom; }
    public LocalDate getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDate validUntil) { this.validUntil = validUntil; }
    public LocalTime getDailyStartsAt() { return dailyStartsAt; }
    public void setDailyStartsAt(LocalTime dailyStartsAt) { this.dailyStartsAt = dailyStartsAt; }
    public LocalTime getDailyEndsAt() { return dailyEndsAt; }
    public void setDailyEndsAt(LocalTime dailyEndsAt) { this.dailyEndsAt = dailyEndsAt; }
    public List<DayOfWeek> getWeekdays() { return weekdays; }
    public void setWeekdays(List<DayOfWeek> weekdays) { this.weekdays = weekdays; }

    public enum AvailabilityType {
        AVAILABLE,
        UNAVAILABLE
    }

    public enum ConstraintKind {
        MANDATORY,
        PREFERENCE
    }
}