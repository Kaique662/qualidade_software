package com.example.educationalqualityproject.auth;

public record PortalUser(String username, String displayName, Role role, String teacherId) {

    public PortalUser(String username, String displayName, Role role) {
        this(username, displayName, role, null);
    }

    public String homePath() {
        return switch (role) {
            case ADMIN -> "/admin";
            case PROFESSOR -> "/professor";
            case RESOURCE_MANAGER -> "/gestora";
        };
    }

    public String roleLabel() {
        return switch (role) {
            case ADMIN -> "Administrador";
            case PROFESSOR -> "Professor";
            case RESOURCE_MANAGER -> "Gestora de espaços e materiais";
        };
    }

    public enum Role {
        ADMIN,
        PROFESSOR,
        RESOURCE_MANAGER
    }
}