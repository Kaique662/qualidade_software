package com.example.educationalqualityproject.controller;

import com.example.educationalqualityproject.auth.PasswordHasher;
import com.example.educationalqualityproject.entity.Teacher;
import com.example.educationalqualityproject.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/teachers")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    @ModelAttribute("activePage")
    public String activePage() {
        return "teachers";
    }

    @GetMapping
    public String listTeachers(Model model) {
        List<Teacher> teachers = teacherService.getAllTeachers();
        model.addAttribute("teachers", teachers);
        return "teacher/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("teacher", new Teacher());
        return "teacher/form";
    }

    @PostMapping
    public String createTeacher(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String department,
            @RequestParam(defaultValue = "") String password,
            Model model) {
        Teacher teacher = new Teacher();
        updateTeacherDetails(teacher, name, email, department);
        if (hasMissingDetails(teacher) || password.length() < 8) {
            model.addAttribute("teacher", teacher);
            model.addAttribute("formError", "Preencha os dados e informe uma senha com pelo menos 8 caracteres.");
            return "teacher/form";
        }
        if (teacherService.existsByEmailIgnoreCase(teacher.getEmail())) {
            model.addAttribute("teacher", teacher);
            model.addAttribute("formError", "Este e-mail já está cadastrado.");
            return "teacher/form";
        }

        teacher.setPasswordHash(PasswordHasher.hash(password));
        teacherService.saveTeacher(teacher);
        return "redirect:/teachers";
    }

    @GetMapping("/{id}/edit")
    public String showUpdateForm(@PathVariable String id, Model model) {
        Teacher teacher = teacherService.getTeacherById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid teacher Id:" + id));
        model.addAttribute("teacher", teacher);
        return "teacher/form";
    }

    @PostMapping("/{id}")
    public String updateTeacher(
            @PathVariable String id,
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String department,
            @RequestParam(defaultValue = "") String password,
            Model model) {
        Teacher teacher = teacherService.getTeacherById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid teacher Id:" + id));
        updateTeacherDetails(teacher, name, email, department);
        if (hasMissingDetails(teacher) || (!password.isEmpty() && password.length() < 8)) {
            model.addAttribute("teacher", teacher);
            model.addAttribute("formError",
                    "Preencha os dados. Uma nova senha deve ter pelo menos 8 caracteres.");
            return "teacher/form";
        }
        if (teacherService.getTeacherByEmail(teacher.getEmail())
                .filter(existing -> !existing.getId().equals(id)).isPresent()) {
            model.addAttribute("teacher", teacher);
            model.addAttribute("formError", "Este e-mail já está cadastrado.");
            return "teacher/form";
        }
        if (!password.isEmpty()) {
            teacher.setPasswordHash(PasswordHasher.hash(password));
        }
        teacherService.saveTeacher(teacher);
        return "redirect:/teachers";
    }

    @PostMapping("/{id}/delete")
    public String deleteTeacher(@PathVariable String id) {
        teacherService.deleteTeacher(id);
        return "redirect:/teachers";
    }

    private void updateTeacherDetails(Teacher teacher, String name, String email, String department) {
        teacher.setName(name.trim());
        teacher.setEmail(email.trim());
        teacher.setDepartment(department.trim());
    }

    private boolean hasMissingDetails(Teacher teacher) {
        return teacher.getName().isBlank() || teacher.getEmail().isBlank()
                || teacher.getDepartment().isBlank();
    }
}