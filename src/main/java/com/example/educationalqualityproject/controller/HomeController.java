package com.example.educationalqualityproject.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @ModelAttribute("activePage")
    public String activePage() {
        return "home";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/admin")
    public String adminHome() {
        return "home";
    }
}