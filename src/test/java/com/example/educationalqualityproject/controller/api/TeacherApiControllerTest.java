package com.example.educationalqualityproject.controller.api;

import com.example.educationalqualityproject.entity.Teacher;
import com.example.educationalqualityproject.service.TeacherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class TeacherApiControllerTest {

    private FakeTeacherService teacherService;
    private TeacherApiController controller;

    @BeforeEach
    void setUp() {
        teacherService = new FakeTeacherService();
        controller = new TeacherApiController();
        ReflectionTestUtils.setField(controller, "teacherService", teacherService);
    }

    @Test
    void shouldListTeachers() {
        List<Teacher> teachers = List.of(new Teacher("Ada", "ada@example.test", "Math"));
        teacherService.teachers = teachers;

        ResponseEntity<List<Teacher>> response = controller.getAllTeachers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(teachers, response.getBody());
    }

    @Test
    void shouldReturnTeacherWhenIdExists() {
        Teacher teacher = new Teacher("Ada", "ada@example.test", "Math");
        teacherService.teachersById.put("teacher-1", teacher);

        ResponseEntity<Teacher> response = controller.getTeacherById("teacher-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(teacher, response.getBody());
    }

    @Test
    void shouldReturnNotFoundWhenTeacherIdDoesNotExist() {
        ResponseEntity<Teacher> response = controller.getTeacherById("missing");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldCreateTeacherWhenEmailIsAvailable() {
        Teacher teacher = new Teacher("Ada", "ada@example.test", "Math");

        ResponseEntity<Teacher> response = controller.createTeacher(teacher);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(teacher, response.getBody());
        assertSame(teacher, teacherService.savedTeacher);
    }

    @Test
    void shouldRejectTeacherWhenEmailAlreadyExists() {
        Teacher teacher = new Teacher("Ada", "ada@example.test", "Math");
        teacherService.emails.add(teacher.getEmail());

        ResponseEntity<Teacher> response = controller.createTeacher(teacher);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(null, teacherService.savedTeacher);
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingMissingTeacher() {
        Teacher teacher = new Teacher("Ada", "ada@example.test", "Math");

        ResponseEntity<Teacher> response = controller.updateTeacher("missing", teacher);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(null, teacherService.savedTeacher);
    }

    @Test
    void shouldRejectUpdateWhenEmailBelongsToAnotherTeacher() {
        Teacher existing = new Teacher("Ada", "ada@example.test", "Math");
        Teacher update = new Teacher("Ada Lovelace", "ada.new@example.test", "Math");
        teacherService.teachersById.put("teacher-1", existing);
        teacherService.emails.add(update.getEmail());

        ResponseEntity<Teacher> response = controller.updateTeacher("teacher-1", update);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(null, teacherService.savedTeacher);
    }

    @Test
    void shouldUpdateTeacherAndPreservePathId() {
        Teacher existing = new Teacher("Ada", "ada@example.test", "Math");
        Teacher update = new Teacher("Ada Lovelace", "ada@example.test", "Physics");
        teacherService.teachersById.put("teacher-1", existing);

        ResponseEntity<Teacher> response = controller.updateTeacher("teacher-1", update);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("teacher-1", response.getBody().getId());
        assertSame(update, teacherService.savedTeacher);
    }

    @Test
    void shouldUpdateTeacherWhenNewEmailIsAvailable() {
        Teacher existing = new Teacher("Ada", "ada@example.test", "Math");
        Teacher update = new Teacher("Ada Lovelace", "ada.new@example.test", "Physics");
        teacherService.teachersById.put("teacher-1", existing);

        ResponseEntity<Teacher> response = controller.updateTeacher("teacher-1", update);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("teacher-1", response.getBody().getId());
        assertSame(update, teacherService.savedTeacher);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingMissingTeacher() {
        ResponseEntity<Void> response = controller.deleteTeacher("missing");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(null, teacherService.deletedId);
    }

    @Test
    void shouldDeleteExistingTeacher() {
        teacherService.teachersById.put("teacher-1", new Teacher("Ada", "ada@example.test", "Math"));

        ResponseEntity<Void> response = controller.deleteTeacher("teacher-1");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertEquals("teacher-1", teacherService.deletedId);
    }

    private static class FakeTeacherService extends TeacherService {
        private List<Teacher> teachers = List.of();
        private final Map<String, Teacher> teachersById = new HashMap<>();
        private final Set<String> emails = new HashSet<>();
        private Teacher savedTeacher;
        private String deletedId;

        @Override
        public List<Teacher> getAllTeachers() {
            return teachers;
        }

        @Override
        public Optional<Teacher> getTeacherById(String id) {
            return Optional.ofNullable(teachersById.get(id));
        }

        @Override
        public Teacher saveTeacher(Teacher teacher) {
            savedTeacher = teacher;
            return teacher;
        }

        @Override
        public void deleteTeacher(String id) {
            deletedId = id;
        }

        @Override
        public boolean existsByEmail(String email) {
            return emails.contains(email);
        }
    }
}
