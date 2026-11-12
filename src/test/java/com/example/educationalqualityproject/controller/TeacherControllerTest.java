package com.example.educationalqualityproject.controller;

import com.example.educationalqualityproject.entity.Teacher;
import com.example.educationalqualityproject.service.TeacherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TeacherControllerTest {

    private FakeTeacherService teacherService;
    private TeacherController controller;

    @BeforeEach
    void setUp() {
        teacherService = new FakeTeacherService();
        controller = new TeacherController();
        ReflectionTestUtils.setField(controller, "teacherService", teacherService);
    }

    @Test
    void shouldListTeachers() {
        List<Teacher> teachers = List.of(new Teacher("Ada", "ada@example.test", "Math"));
        teacherService.teachers = teachers;
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.listTeachers(model);

        assertEquals("teacher/list", view);
        assertSame(teachers, model.get("teachers"));
    }

    @Test
    void shouldShowCreateForm() {
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.showCreateForm(model);

        assertEquals("teacher/form", view);
        assertInstanceOf(Teacher.class, model.get("teacher"));
    }

    @Test
    void shouldCreateTeacherAndRedirectToList() {
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.createTeacher(
                "Ada", "ada@example.test", "Math", "teacher-secret", model);

        assertEquals("redirect:/teachers", view);
        assertEquals("Ada", teacherService.savedTeacher.getName());
        assertEquals("ada@example.test", teacherService.savedTeacher.getEmail());
        assertEquals("Math", teacherService.savedTeacher.getDepartment());
        assertTrue(com.example.educationalqualityproject.auth.PasswordHasher.matches(
                "teacher-secret", teacherService.savedTeacher.getPasswordHash()));
    }

    @Test
    void shouldShowEditFormForExistingTeacher() {
        Teacher teacher = new Teacher("Ada", "ada@example.test", "Math");
        teacherService.teachersById.put("teacher-1", teacher);
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.showUpdateForm("teacher-1", model);

        assertEquals("teacher/form", view);
        assertSame(teacher, model.get("teacher"));
    }

    @Test
    void shouldRejectEditFormWhenTeacherDoesNotExist() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.showUpdateForm("missing", new ExtendedModelMap()));
    }

    @Test
    void shouldUpdateTeacherAndRedirectToList() {
        Teacher teacher = new Teacher("Ada", "ada@example.test", "Math");
        teacher.setId("teacher-1");
        teacher.setPasswordHash(com.example.educationalqualityproject.auth.PasswordHasher.hash("old-secret"));
        teacherService.teachersById.put("teacher-1", teacher);

        String view = controller.updateTeacher(
                "teacher-1", "Ada Lovelace", "ada@example.test", "Math", "", new ExtendedModelMap());

        assertEquals("redirect:/teachers", view);
        assertEquals("teacher-1", teacher.getId());
        assertSame(teacher, teacherService.savedTeacher);
        assertTrue(com.example.educationalqualityproject.auth.PasswordHasher.matches(
                "old-secret", teacher.getPasswordHash()));
    }

    @Test
    void shouldRejectDuplicateEmailWhenCreatingTeacher() {
        teacherService.existingTeacher = new Teacher("Grace", "ada@example.test", "Math");
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.createTeacher(
                "Ada", "ada@example.test", "Math", "teacher-secret", model);

        assertEquals("teacher/form", view);
        assertEquals("Este e-mail já está cadastrado.", model.get("formError"));
        assertEquals(null, teacherService.savedTeacher);
    }

    @Test
    void shouldRejectShortPasswordWhenCreatingTeacher() {
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.createTeacher("Ada", "ada@example.test", "Math", "short", model);

        assertEquals("teacher/form", view);
        assertEquals(null, teacherService.savedTeacher);
    }

    @Test
    void shouldDeleteTeacherAndRedirectToList() {
        String view = controller.deleteTeacher("teacher-1");

        assertEquals("redirect:/teachers", view);
        assertEquals("teacher-1", teacherService.deletedId);
    }

    private static class FakeTeacherService extends TeacherService {
        private List<Teacher> teachers = List.of();
        private final Map<String, Teacher> teachersById = new HashMap<>();
        private Teacher savedTeacher;
        private String deletedId;
        private Teacher existingTeacher;

        @Override
        public List<Teacher> getAllTeachers() {
            return teachers;
        }

        @Override
        public Optional<Teacher> getTeacherById(String id) {
            return Optional.ofNullable(teachersById.get(id));
        }

        @Override
        public Optional<Teacher> getTeacherByEmail(String email) {
            if (existingTeacher != null && existingTeacher.getEmail().equalsIgnoreCase(email)) {
                return Optional.of(existingTeacher);
            }
            return teachersById.values().stream()
                    .filter(teacher -> teacher.getEmail().equalsIgnoreCase(email)).findFirst();
        }

        @Override
        public boolean existsByEmailIgnoreCase(String email) {
            return getTeacherByEmail(email).isPresent();
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
    }
}
