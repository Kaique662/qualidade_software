package com.example.educationalqualityproject.service;

import com.example.educationalqualityproject.auth.PasswordHasher;
import com.example.educationalqualityproject.entity.Teacher;
import com.example.educationalqualityproject.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TeacherService {

    @Autowired
    private TeacherRepository teacherRepository;

    public List<Teacher> getAllTeachers() {
        return teacherRepository.findAll();
    }

    public Optional<Teacher> getTeacherById(String id) {
        return teacherRepository.findById(id);
    }

    public Optional<Teacher> getTeacherByEmail(String email) {
        return teacherRepository.findByEmailIgnoreCase(email);
    }

    public Teacher saveTeacher(Teacher teacher) {
        return teacherRepository.save(teacher);
    }

    public void deleteTeacher(String id) {
        teacherRepository.deleteById(id);
    }

    public boolean existsByEmail(String email) {
        return teacherRepository.existsByEmail(email);
    }

    public boolean existsByEmailIgnoreCase(String email) {
        return teacherRepository.existsByEmailIgnoreCase(email);
    }

    public boolean changePassword(String teacherId, String currentPassword, String newPassword) {
        Optional<Teacher> teacherResult = teacherRepository.findById(teacherId);
        if (teacherResult.isEmpty()) {
            return false;
        }

        Teacher teacher = teacherResult.get();
        if (!PasswordHasher.matches(currentPassword, teacher.getPasswordHash())) {
            return false;
        }

        teacher.setPasswordHash(PasswordHasher.hash(newPassword));
        teacherRepository.save(teacher);
        return true;
    }
}