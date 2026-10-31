package com.example.educationalqualityproject.controller;

import com.example.educationalqualityproject.entity.Student;
import com.example.educationalqualityproject.service.StudentService;
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

class StudentControllerTest {

    private FakeStudentService studentService;
    private StudentController controller;

    @BeforeEach
    void setUp() {
        studentService = new FakeStudentService();
        controller = new StudentController();
        ReflectionTestUtils.setField(controller, "studentService", studentService);
    }

    @Test
    void shouldListStudents() {
        List<Student> students = List.of(new Student("Ada", "ada@example.test", "S-1"));
        studentService.students = students;
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.listStudents(model);

        assertEquals("student/list", view);
        assertSame(students, model.get("students"));
    }

    @Test
    void shouldShowCreateForm() {
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.showCreateForm(model);

        assertEquals("student/form", view);
        assertInstanceOf(Student.class, model.get("student"));
    }

    @Test
    void shouldCreateStudentAndRedirectToList() {
        Student student = new Student("Ada", "ada@example.test", "S-1");

        String view = controller.createStudent(student);

        assertEquals("redirect:/students", view);
        assertSame(student, studentService.savedStudent);
    }

    @Test
    void shouldShowEditFormForExistingStudent() {
        Student student = new Student("Ada", "ada@example.test", "S-1");
        studentService.studentsById.put("student-1", student);
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.showUpdateForm("student-1", model);

        assertEquals("student/form", view);
        assertSame(student, model.get("student"));
    }

    @Test
    void shouldRejectEditFormWhenStudentDoesNotExist() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.showUpdateForm("missing", new ExtendedModelMap()));
    }

    @Test
    void shouldUpdateStudentAndRedirectToList() {
        Student student = new Student("Ada Lovelace", "ada@example.test", "S-1");

        String view = controller.updateStudent("student-1", student);

        assertEquals("redirect:/students", view);
        assertEquals("student-1", student.getId());
        assertSame(student, studentService.savedStudent);
    }

    @Test
    void shouldDeleteStudentAndRedirectToList() {
        String view = controller.deleteStudent("student-1");

        assertEquals("redirect:/students", view);
        assertEquals("student-1", studentService.deletedId);
    }

    private static class FakeStudentService extends StudentService {
        private List<Student> students = List.of();
        private final Map<String, Student> studentsById = new HashMap<>();
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
    }
}
