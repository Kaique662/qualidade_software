package com.example.educationalqualityproject.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortalLoginServiceTest {

    @Test
    void authenticatesConfiguredAccountsWithTheirRoles() {
        PortalLoginService service = new PortalLoginService(
                "admin", "admin-secret", "marcos", "professor-secret", "renata", "manager-secret");

        assertEquals(PortalUser.Role.ADMIN, service.authenticate("admin", "admin-secret").orElseThrow().role());
        assertEquals(PortalUser.Role.PROFESSOR, service.authenticate("marcos", "professor-secret").orElseThrow().role());
        assertEquals(PortalUser.Role.RESOURCE_MANAGER, service.authenticate("renata", "manager-secret").orElseThrow().role());
        assertTrue(service.authenticate("admin", "wrong-password").isEmpty());
    }

    @Test
    void doesNotEnableAccountsWithoutPasswords() {
        PortalLoginService service = new PortalLoginService(
                "admin", "", "professor", "", "gestora", "");

        assertTrue(service.authenticate("admin", "anything").isEmpty());
    }
}