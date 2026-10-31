package com.example.educationalqualityproject.service;

import com.example.educationalqualityproject.entity.Teacher;
import com.example.educationalqualityproject.repository.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class TeacherServiceTest {

    private FakeTeacherRepository repository;
    private TeacherService service;

    @BeforeEach
    void setUp() {
        repository = new FakeTeacherRepository();
        service = new TeacherService();
        ReflectionTestUtils.setField(service, "teacherRepository", repository.asRepository());
    }

    @Test
    void shouldReturnAllTeachersFromRepository() {
        List<Teacher> teachers = List.of(new Teacher("Ada", "ada@example.test", "Math"));
        repository.teachers.addAll(teachers);

        assertEquals(teachers, service.getAllTeachers());
    }

    @Test
    void shouldReturnTeacherWhenRepositoryFindsId() {
        Teacher teacher = new Teacher("Ada", "ada@example.test", "Math");
        repository.teachersById.put("teacher-1", teacher);

        assertEquals(Optional.of(teacher), service.getTeacherById("teacher-1"));
    }

    @Test
    void shouldReturnEmptyWhenRepositoryDoesNotFindId() {
        assertEquals(Optional.empty(), service.getTeacherById("missing"));
    }

    @Test
    void shouldReturnSavedTeacher() {
        Teacher teacher = new Teacher("Ada", "ada@example.test", "Math");

        assertSame(teacher, service.saveTeacher(teacher));
        assertSame(teacher, repository.savedTeacher);
    }

    @Test
    void shouldDeleteTeacherById() {
        service.deleteTeacher("teacher-1");

        assertEquals("teacher-1", repository.deletedId);
    }

    @Test
    void shouldReportWhetherEmailExists() {
        repository.emails.add("ada@example.test");

        assertEquals(true, service.existsByEmail("ada@example.test"));
        assertEquals(false, service.existsByEmail("other@example.test"));
    }

    private static class FakeTeacherRepository implements InvocationHandler {
        private final List<Teacher> teachers = new ArrayList<>();
        private final Map<String, Teacher> teachersById = new HashMap<>();
        private final Set<String> emails = new HashSet<>();
        private Teacher savedTeacher;
        private String deletedId;

        private TeacherRepository asRepository() {
            return (TeacherRepository) Proxy.newProxyInstance(
                    TeacherRepository.class.getClassLoader(),
                    new Class<?>[]{TeacherRepository.class},
                    this);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] arguments) {
            return switch (method.getName()) {
                case "findAll" -> teachers;
                case "findById" -> Optional.ofNullable(teachersById.get(arguments[0]));
                case "save" -> save((Teacher) arguments[0]);
                case "deleteById" -> delete((String) arguments[0]);
                case "existsByEmail" -> emails.contains(arguments[0]);
                default -> throw new UnsupportedOperationException(method.getName());
            };
        }

        private Teacher save(Teacher teacher) {
            savedTeacher = teacher;
            return teacher;
        }

        private Void delete(String id) {
            deletedId = id;
            return null;
        }
    }
}
