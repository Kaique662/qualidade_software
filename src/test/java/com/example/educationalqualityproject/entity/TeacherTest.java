package com.example.educationalqualityproject.entity;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TeacherTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2025-03-08T12:30:00Z"), ZoneOffset.UTC);
    private static final LocalDateTime NOW = LocalDateTime.of(2025, 3, 8, 12, 30);

    @Test
    void shouldInitializeTimestampsFromInjectedClock() {
        Teacher teacher = new Teacher("Ada", "ada@example.test", "Math", FIXED_CLOCK);

        assertEquals("Ada", teacher.getName());
        assertEquals("ada@example.test", teacher.getEmail());
        assertEquals("Math", teacher.getDepartment());
        assertEquals(NOW, teacher.getCreatedAt());
        assertEquals(NOW, teacher.getUpdatedAt());
    }

    @Test
    void shouldUpdateTimestampWhenMutableTeacherFieldsChange() {
        Teacher teacher = new Teacher(FIXED_CLOCK);
        LocalDateTime earlier = LocalDateTime.of(2025, 3, 7, 12, 30);
        teacher.setUpdatedAt(earlier);

        teacher.setId("teacher-1");
        teacher.setName("Ada Lovelace");
        teacher.setEmail("ada.new@example.test");
        teacher.setDepartment("Physics");

        assertEquals("teacher-1", teacher.getId());
        assertEquals("Ada Lovelace", teacher.getName());
        assertEquals("ada.new@example.test", teacher.getEmail());
        assertEquals("Physics", teacher.getDepartment());
        assertEquals(NOW, teacher.getUpdatedAt());
    }

    @Test
    void shouldAllowExplicitTimestampAssignment() {
        Teacher teacher = new Teacher(FIXED_CLOCK);
        LocalDateTime created = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime updated = LocalDateTime.of(2025, 2, 1, 0, 0);

        teacher.setCreatedAt(created);
        teacher.setUpdatedAt(updated);

        assertEquals(created, teacher.getCreatedAt());
        assertEquals(updated, teacher.getUpdatedAt());
    }
}
