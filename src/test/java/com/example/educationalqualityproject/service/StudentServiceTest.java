package com.example.educationalqualityproject.service;

import com.example.educationalqualityproject.entity.Student;
import com.example.educationalqualityproject.repository.StudentRepository;
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

class StudentServiceTest {

    private FakeStudentRepository repository;
    private StudentService service;

    @BeforeEach
    void setUp() {
        repository = new FakeStudentRepository();
        service = new StudentService();
        ReflectionTestUtils.setField(service, "studentRepository", repository.asRepository());
    }

    @Test
    void shouldReturnAllStudentsFromRepository() {
        List<Student> students = List.of(new Student("Ada", "ada@example.test", "S-1"));
        repository.students.addAll(students);

        assertEquals(students, service.getAllStudents());
    }

    @Test
    void shouldReturnStudentWhenRepositoryFindsId() {
        Student student = new Student("Ada", "ada@example.test", "S-1");
        repository.studentsById.put("student-1", student);

        assertEquals(Optional.of(student), service.getStudentById("student-1"));
    }

    @Test
    void shouldReturnEmptyWhenRepositoryDoesNotFindId() {
        assertEquals(Optional.empty(), service.getStudentById("missing"));
    }

    @Test
    void shouldReturnSavedStudent() {
        Student student = new Student("Ada", "ada@example.test", "S-1");

        assertSame(student, service.saveStudent(student));
        assertSame(student, repository.savedStudent);
    }

    @Test
    void shouldDeleteStudentById() {
        service.deleteStudent("student-1");

        assertEquals("student-1", repository.deletedId);
    }

    @Test
    void shouldReportWhetherEmailExists() {
        repository.emails.add("ada@example.test");

        assertEquals(true, service.existsByEmail("ada@example.test"));
        assertEquals(false, service.existsByEmail("other@example.test"));
    }

    @Test
    void shouldReportWhetherRegistrationNumberExists() {
        repository.registrationNumbers.add("S-1");

        assertEquals(true, service.existsByRegistrationNumber("S-1"));
        assertEquals(false, service.existsByRegistrationNumber("S-2"));
    }

    private static class FakeStudentRepository implements InvocationHandler {
        private final List<Student> students = new ArrayList<>();
        private final Map<String, Student> studentsById = new HashMap<>();
        private final Set<String> emails = new HashSet<>();
        private final Set<String> registrationNumbers = new HashSet<>();
        private Student savedStudent;
        private String deletedId;

        private StudentRepository asRepository() {
            return (StudentRepository) Proxy.newProxyInstance(
                    StudentRepository.class.getClassLoader(),
                    new Class<?>[]{StudentRepository.class},
                    this);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] arguments) {
            return switch (method.getName()) {
                case "findAll" -> students;
                case "findById" -> Optional.ofNullable(studentsById.get(arguments[0]));
                case "save" -> save((Student) arguments[0]);
                case "deleteById" -> delete((String) arguments[0]);
                case "existsByEmail" -> emails.contains(arguments[0]);
                case "existsByRegistrationNumber" -> registrationNumbers.contains(arguments[0]);
                default -> throw new UnsupportedOperationException(method.getName());
            };
        }

        private Student save(Student student) {
            savedStudent = student;
            return student;
        }

        private Void delete(String id) {
            deletedId = id;
            return null;
        }
    }
}
