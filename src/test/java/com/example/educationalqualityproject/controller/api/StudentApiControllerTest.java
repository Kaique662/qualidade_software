package com.example.educationalqualityproject.controller.api;

import com.example.educationalqualityproject.entity.Student;
import com.example.educationalqualityproject.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class StudentApiControllerTest {

    private FakeStudentService studentService;

    private StudentApiController controller;

    @BeforeEach
    void setUp() {
        studentService = new FakeStudentService();
        controller = new StudentApiController();
        ReflectionTestUtils.setField(controller, "studentService", studentService);
    }

    @Test
    void shouldListStudents() {
        List<Student> students = List.of(new Student("Ada", "ada@example.test", "S-1"));
        studentService.students = students;

        ResponseEntity<List<Student>> response = controller.getAllStudents();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(students, response.getBody());
    }

    @Test
    void shouldReturnStudentWhenIdExists() {
        Student student = new Student("Ada", "ada@example.test", "S-1");
        studentService.studentsById.put("student-1", student);

        ResponseEntity<Student> response = controller.getStudentById("student-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(student, response.getBody());
    }

    @Test
    void shouldReturnNotFoundWhenStudentIdDoesNotExist() {
        ResponseEntity<Student> response = controller.getStudentById("missing");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldCreateStudentWhenUniqueFieldsAreAvailable() {
        Student student = new Student("Ada", "ada@example.test", "S-1");

        ResponseEntity<Student> response = controller.createStudent(student);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(student, response.getBody());
        assertSame(student, studentService.savedStudent);
    }

    @Test
    void shouldRejectStudentWhenEmailAlreadyExists() {
        Student student = new Student("Ada", "ada@example.test", "S-1");
        studentService.emails.add(student.getEmail());

        ResponseEntity<Student> response = controller.createStudent(student);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(null, studentService.savedStudent);
    }

    @Test
    void shouldRejectStudentWhenRegistrationNumberAlreadyExists() {
        Student student = new Student("Ada", "ada@example.test", "S-1");
        studentService.registrationNumbers.add(student.getRegistrationNumber());

        ResponseEntity<Student> response = controller.createStudent(student);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(null, studentService.savedStudent);
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingMissingStudent() {
        Student student = new Student("Ada", "ada@example.test", "S-1");

        ResponseEntity<Student> response = controller.updateStudent("missing", student);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(null, studentService.savedStudent);
    }

    @Test
    void shouldRejectUpdateWhenEmailBelongsToAnotherStudent() {
        Student existing = new Student("Ada", "ada@example.test", "S-1");
        Student update = new Student("Ada Lovelace", "ada.new@example.test", "S-2");
        studentService.studentsById.put("student-1", existing);
        studentService.emails.add(update.getEmail());

        ResponseEntity<Student> response = controller.updateStudent("student-1", update);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(null, studentService.savedStudent);
    }

    @Test
    void shouldRejectUpdateWhenRegistrationNumberBelongsToAnotherStudent() {
        Student existing = new Student("Ada", "ada@example.test", "S-1");
        Student update = new Student("Ada Lovelace", "ada.new@example.test", "S-2");
        studentService.studentsById.put("student-1", existing);
        studentService.registrationNumbers.add(update.getRegistrationNumber());

        ResponseEntity<Student> response = controller.updateStudent("student-1", update);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(null, studentService.savedStudent);
    }

    @Test
    void shouldUpdateStudentAndPreservePathId() {
        Student existing = new Student("Ada", "ada@example.test", "S-1");
        Student update = new Student("Ada Lovelace", "ada@example.test", "S-1");
        studentService.studentsById.put("student-1", existing);

        ResponseEntity<Student> response = controller.updateStudent("student-1", update);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("student-1", response.getBody().getId());
        assertSame(update, studentService.savedStudent);
    }

    @Test
    void shouldUpdateStudentWhenNewEmailAndRegistrationNumberAreAvailable() {
        Student existing = new Student("Ada", "ada@example.test", "S-1");
        Student update = new Student("Ada Lovelace", "ada.new@example.test", "S-2");
        studentService.studentsById.put("student-1", existing);

        ResponseEntity<Student> response = controller.updateStudent("student-1", update);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("student-1", response.getBody().getId());
        assertSame(update, studentService.savedStudent);
    }

    @Test
    void shouldDeleteExistingStudent() {
        Student existing = new Student("Ada", "ada@example.test", "S-1");
        studentService.studentsById.put("student-1", existing);

        ResponseEntity<Void> response = controller.deleteStudent("student-1");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertEquals("student-1", studentService.deletedId);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingMissingStudent() {
        ResponseEntity<Void> response = controller.deleteStudent("missing");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(null, studentService.deletedId);
    }

    private static class FakeStudentService extends StudentService {
        private List<Student> students = List.of();
        private final Map<String, Student> studentsById = new HashMap<>();
        private final Set<String> emails = new HashSet<>();
        private final Set<String> registrationNumbers = new HashSet<>();
        private Student savedStudent;
        private String deletedId;

        @Override
        public List<Student> getAllStudents() {
            return students;
        }

        @Override
        public Optional<Student> getStudentById(String id) {
            return Optional.ofNullable(studentsById.get(id));
        }

        @Override
        public Student saveStudent(Student student) {
            savedStudent = student;
            return student;
        }

        @Override
        public void deleteStudent(String id) {
            deletedId = id;
        }

        @Override
        public boolean existsByEmail(String email) {
            return emails.contains(email);
        }

        @Override
        public boolean existsByRegistrationNumber(String registrationNumber) {
            return registrationNumbers.contains(registrationNumber);
        }
    }
}
