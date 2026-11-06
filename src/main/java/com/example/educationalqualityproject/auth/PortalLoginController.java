package com.example.educationalqualityproject.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PortalLoginController {

    private final PortalLoginService loginService;

    public PortalLoginController(PortalLoginService loginService) {
        this.loginService = loginService;
    }

    @GetMapping("/login")
    public String login(HttpSession session) {
        PortalUser user = (PortalUser) session.getAttribute("portalUser");
        return user == null ? "auth/login" : "redirect:" + user.homePath();
    }

    @PostMapping("/login")
    public String authenticate(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletRequest request,
            Model model) {
        return loginService.authenticate(username.trim(), password)
                .map(user -> {
                    HttpSession previousSession = request.getSession(false);
                    if (previousSession != null) {
                        previousSession.invalidate();
                    }
                    request.getSession(true).setAttribute("portalUser", user);
                    return "redirect:" + user.homePath();
                })
                .orElseGet(() -> {
                    model.addAttribute("loginError", "Usuário ou senha inválidos.");
                    model.addAttribute("username", username.trim());
                    return "auth/login";
                });
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}