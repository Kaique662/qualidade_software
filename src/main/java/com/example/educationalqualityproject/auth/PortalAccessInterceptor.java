package com.example.educationalqualityproject.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

public class PortalAccessInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (isPublicPath(path)) {
            return true;
        }

        HttpSession session = request.getSession(false);
        PortalUser user = session == null ? null : (PortalUser) session.getAttribute("portalUser");
        if (user == null) {
            if (path.startsWith("/api/")) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            } else {
                response.sendRedirect(request.getContextPath() + "/login?required");
            }
            return false;
        }

        request.setAttribute("portalUser", user);
        if (!isAllowed(path, request.getMethod(), user.role())) {
            if (path.startsWith("/api/")) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            } else {
                response.sendRedirect(request.getContextPath() + user.homePath());
            }
            return false;
        }
        return true;
    }

    private boolean isPublicPath(String path) {
        return path.equals("/") || path.equals("/login") || path.equals("/logout") || path.equals("/error")
                || path.equals("/favicon.ico") || path.startsWith("/css/")
                || path.startsWith("/js/") || path.startsWith("/webjars/");
    }

    private boolean isAllowed(String path, String method, PortalUser.Role role) {
        if (path.equals("/admin") || path.startsWith("/students/") || path.equals("/students")
                || path.startsWith("/teachers/") || path.equals("/teachers")) {
            return role == PortalUser.Role.ADMIN;
        }
        if (path.equals("/professor") || path.startsWith("/professor/")) {
            return role == PortalUser.Role.PROFESSOR;
        }
        if (path.equals("/gestora") || path.startsWith("/gestora/")) {
            return role == PortalUser.Role.RESOURCE_MANAGER;
        }
        if (!path.startsWith("/api/")) {
            return false;
        }
        if (path.startsWith("/api/resources") || path.startsWith("/api/resource-blocks")) {
            return role == PortalUser.Role.ADMIN || role == PortalUser.Role.RESOURCE_MANAGER;
        }
        if (path.startsWith("/api/teachers/") && path.contains("/availability")) {
            return role == PortalUser.Role.ADMIN;
        }
        if (path.startsWith("/api/schedules/") && method.equals("GET")) {
            return role == PortalUser.Role.ADMIN || role == PortalUser.Role.PROFESSOR
                    || role == PortalUser.Role.RESOURCE_MANAGER;
        }
        return role == PortalUser.Role.ADMIN;
    }
}