package com.example.educationalqualityproject.controller;

import com.example.educationalqualityproject.auth.PortalUser;
import com.example.educationalqualityproject.service.TeacherService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TeacherAccountController {

    private final TeacherService teacherService;

    public TeacherAccountController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @PostMapping("/professor/password")
    public String changePassword(
            @RequestAttribute("portalUser") PortalUser user,
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword) {
        if (newPassword.length() < 8) {
            return "redirect:/professor?passwordError=weak";
        }
        if (!newPassword.equals(confirmPassword)) {
            return "redirect:/professor?passwordError=mismatch";
        }
        if (user.teacherId() == null
                || !teacherService.changePassword(user.teacherId(), currentPassword, newPassword)) {
            return "redirect:/professor?passwordError=invalid";
        }
        return "redirect:/professor?passwordChanged";
    }
}
