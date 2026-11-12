package com.example.educationalqualityproject.auth;

import com.example.educationalqualityproject.entity.Teacher;
import com.example.educationalqualityproject.repository.TeacherRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
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
    void includesConfiguredTeacherRecordIdForProfessorAccount() {
        PortalLoginService service = new PortalLoginService(
                "admin", "admin-secret", "professor", "prof-secret", "teacher-123",
                "gestora", "manager-secret");

        PortalUser professor = service.authenticate("professor", "prof-secret").orElseThrow();

        assertEquals("teacher-123", professor.teacherId());
    }

    @Test
    void doesNotEnableAccountsWithoutPasswords() {
        PortalLoginService service = new PortalLoginService(
                "admin", "", "professor", "", "gestora", "");

        assertTrue(service.authenticate("admin", "anything").isEmpty());
    }

    @Test
    void authenticatesRegisteredTeacherByEmailAndPasswordHash() {
        TeacherRepository repository = mock(TeacherRepository.class);
        Teacher teacher = new Teacher("Ada", "ada@example.test", "Math");
        teacher.setId("teacher-1");
        teacher.setPasswordHash(PasswordHasher.hash("teacher-secret"));
        when(repository.findByEmailIgnoreCase("ada@example.test")).thenReturn(Optional.of(teacher));
        PortalLoginService service = new PortalLoginService(
                "admin", "admin-secret", "professor", "", "", "gestora", "", repository);

        PortalUser user = service.authenticate("ada@example.test", "teacher-secret").orElseThrow();

        assertEquals(PortalUser.Role.PROFESSOR, user.role());
        assertEquals("teacher-1", user.teacherId());
        assertEquals("Ada", user.displayName());
        assertTrue(service.authenticate("ada@example.test", "wrong").isEmpty());
    }
}