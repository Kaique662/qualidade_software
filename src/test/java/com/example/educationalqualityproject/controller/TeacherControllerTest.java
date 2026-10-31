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
        Teacher teacher = new Teacher("Ada", "ada@example.test", "Math");

        String view = controller.createTeacher(teacher);

        assertEquals("redirect:/teachers", view);
        assertSame(teacher, teacherService.savedTeacher);
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
        Teacher teacher = new Teacher("Ada Lovelace", "ada@example.test", "Math");

        String view = controller.updateTeacher("teacher-1", teacher);

        assertEquals("redirect:/teachers", view);
        assertEquals("teacher-1", teacher.getId());
        assertSame(teacher, teacherService.savedTeacher);
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
    }
}
