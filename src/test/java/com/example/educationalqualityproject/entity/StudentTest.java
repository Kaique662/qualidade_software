package com.example.educationalqualityproject.entity;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StudentTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2025-03-08T12:30:00Z"), ZoneOffset.UTC);
    private static final LocalDateTime NOW = LocalDateTime.of(2025, 3, 8, 12, 30);

    @Test
    void shouldInitializeTimestampsFromInjectedClock() {
        Student student = new Student("Ada", "ada@example.test", "S-1", FIXED_CLOCK);

        assertEquals("Ada", student.getName());
        assertEquals("ada@example.test", student.getEmail());
        assertEquals("S-1", student.getRegistrationNumber());
        assertEquals(NOW, student.getCreatedAt());
        assertEquals(NOW, student.getUpdatedAt());
    }

    @Test
    void shouldUpdateTimestampWhenMutableStudentFieldsChange() {
        Student student = new Student(FIXED_CLOCK);
        LocalDateTime earlier = LocalDateTime.of(2025, 3, 7, 12, 30);
        student.setUpdatedAt(earlier);

        student.setId("student-1");
        student.setName("Ada Lovelace");
        student.setEmail("ada.new@example.test");
        student.setRegistrationNumber("S-2");

        assertEquals("student-1", student.getId());
        assertEquals("Ada Lovelace", student.getName());
        assertEquals("ada.new@example.test", student.getEmail());
        assertEquals("S-2", student.getRegistrationNumber());
        assertEquals(NOW, student.getUpdatedAt());
    }

    @Test
    void shouldAllowExplicitTimestampAssignment() {
        Student student = new Student(FIXED_CLOCK);
        LocalDateTime created = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime updated = LocalDateTime.of(2025, 2, 1, 0, 0);

        student.setCreatedAt(created);
        student.setUpdatedAt(updated);

        assertEquals(created, student.getCreatedAt());
        assertEquals(updated, student.getUpdatedAt());
    }
}
