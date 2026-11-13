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
        if (!isAllowed(path, request, user)) {
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

    private boolean isAllowed(String path, HttpServletRequest request, PortalUser user) {
        PortalUser.Role role = user.role();
        String method = request.getMethod();

        // Permite à Gestora consultar cursos, turmas e disciplinas
        if ("GET".equals(method) &&
                (path.equals("/api/courses") ||
                        path.equals("/api/classes") ||
                        path.equals("/api/subjects"))) {

            return role == PortalUser.Role.ADMIN ||
                    role == PortalUser.Role.RESOURCE_MANAGER;
        }

        // Áreas administrativas continuam exclusivas do ADMIN
        if (path.equals("/admin") || path.startsWith("/admin/")
                || path.startsWith("/students/") || path.equals("/students")
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
        if (request.getMethod().equals("GET")
                && (path.equals("/api/academic-periods") || path.startsWith("/api/academic-periods/"))) {
            return true;
        }
        if (path.startsWith("/api/resources") || path.startsWith("/api/resource-blocks")) {
            return role == PortalUser.Role.ADMIN || role == PortalUser.Role.RESOURCE_MANAGER;
        }
        if (path.startsWith("/api/teachers/") && path.contains("/availability")) {
            return role == PortalUser.Role.ADMIN
                    || role == PortalUser.Role.PROFESSOR && matchesTeacherPath(path, user.teacherId());
        }
        if (path.startsWith("/api/teachers/") && path.endsWith("/schedules")) {
            return role == PortalUser.Role.ADMIN
                    || role == PortalUser.Role.PROFESSOR && matchesTeacherPath(path, user.teacherId());
        }
        if (path.startsWith("/api/schedules/") && path.endsWith("/adjustment-requests")) {

            if (path.startsWith("/api/schedules/") &&
                    path.endsWith("/adjustment-requests")) {

                return role == PortalUser.Role.ADMIN
                        || role == PortalUser.Role.RESOURCE_MANAGER
                        || (role == PortalUser.Role.PROFESSOR
                                && hasTeacherLink(user));
            }

            return role == PortalUser.Role.ADMIN
                    || role == PortalUser.Role.PROFESSOR && hasTeacherLink(user);
        }
        if (path.startsWith("/api/schedules/") && path.contains("/notifications")) {
            return role == PortalUser.Role.ADMIN || role == PortalUser.Role.RESOURCE_MANAGER
                    || role == PortalUser.Role.PROFESSOR
                            && user.teacherId() != null
                            && user.teacherId().equals(request.getParameter("recipient"));
        }
        if (path.startsWith("/api/schedules/") && method.equals("GET")) {
            return role == PortalUser.Role.ADMIN || role == PortalUser.Role.RESOURCE_MANAGER;
        }
        return role == PortalUser.Role.ADMIN;
    }

    private boolean matchesTeacherPath(String path, String teacherId) {
        if (teacherId == null || teacherId.isBlank()) {
            return false;
        }
        String prefix = "/api/teachers/";
        int end = path.indexOf('/', prefix.length());
        String requestedTeacherId = end < 0 ? path.substring(prefix.length()) : path.substring(prefix.length(), end);
        return teacherId.equals(requestedTeacherId);
    }

    private boolean hasTeacherLink(PortalUser user) {
        return user.teacherId() != null && !user.teacherId().isBlank();
    }
}