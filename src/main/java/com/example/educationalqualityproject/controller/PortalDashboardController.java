package com.example.educationalqualityproject.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class PortalDashboardController {

    @GetMapping("/professor")
    public String professorDashboard() {
        return "portal/professor";
    }

    @GetMapping("/gestora")
    public String resourceManagerDashboard() {
        return "portal/gestora";
    }

    @GetMapping("/admin/planning")
    public String academicPlanningDashboard() {
        return "portal/academic-planning";
    }

    @ModelAttribute("activePage")
    public String activePage(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.endsWith("/gestora")) {
            return "gestora";
        }
        return path.endsWith("/admin/planning") ? "planning" : "professor";
    }
}