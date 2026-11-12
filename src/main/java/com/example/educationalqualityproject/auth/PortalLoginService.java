package com.example.educationalqualityproject.auth;

import com.example.educationalqualityproject.entity.Teacher;
import com.example.educationalqualityproject.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Optional;

@Service
public class PortalLoginService {

    private final List<Account> accounts;
    private final TeacherRepository teacherRepository;

    public PortalLoginService(String adminUsername, String adminPassword,
                              String professorUsername, String professorPassword,
                              String managerUsername, String managerPassword) {
        this(adminUsername, adminPassword, professorUsername, professorPassword, "",
                managerUsername, managerPassword, null);
    }

    public PortalLoginService(String adminUsername, String adminPassword,
                              String professorUsername, String professorPassword,
                              String professorTeacherId,
                              String managerUsername, String managerPassword) {
        this(adminUsername, adminPassword, professorUsername, professorPassword, professorTeacherId,
                managerUsername, managerPassword, null);
    }

    @Autowired
    public PortalLoginService(
            @Value("${app.auth.admin.username:${APP_ADMIN_USERNAME:admin}}") String adminUsername,
            @Value("${app.auth.admin.password:${APP_ADMIN_PASSWORD:}}") String adminPassword,
            @Value("${app.auth.professor.username:${APP_PROFESSOR_USERNAME:professor}}") String professorUsername,
            @Value("${app.auth.professor.password:${APP_PROFESSOR_PASSWORD:}}") String professorPassword,
            @Value("${app.auth.professor.teacher-id:${APP_PROFESSOR_TEACHER_ID:}}") String professorTeacherId,
            @Value("${app.auth.manager.username:${APP_MANAGER_USERNAME:gestora}}") String managerUsername,
            @Value("${app.auth.manager.password:${APP_MANAGER_PASSWORD:}}") String managerPassword,
            TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
        this.accounts = List.of(
                new Account(adminUsername, adminPassword, "Administrador", PortalUser.Role.ADMIN, null),
                new Account(professorUsername, professorPassword, "Marcos Vinícius Lima",
                    PortalUser.Role.PROFESSOR, professorTeacherId),
                new Account(managerUsername, managerPassword, "Renata Costa Nascimento",
                    PortalUser.Role.RESOURCE_MANAGER, null));
    }

    public Optional<PortalUser> authenticate(String username, String password) {
        if (username == null || password == null) {
            return Optional.empty();
        }

        Optional<PortalUser> configuredAccount = accounts.stream()
                .filter(account -> !account.username().isBlank() && !account.password().isBlank())
                .filter(account -> matches(account.username(), username)
                        && matches(account.password(), password))
                .map(account -> new PortalUser(account.username(), account.displayName(),
                    account.role(), account.teacherId()))
                .findFirst();
        if (configuredAccount.isPresent() || teacherRepository == null) {
            return configuredAccount;
        }
        return teacherRepository.findByEmailIgnoreCase(username)
                .filter(teacher -> PasswordHasher.matches(password, teacher.getPasswordHash()))
                .map(teacher -> new PortalUser(
                        teacher.getEmail(), teacher.getName(), PortalUser.Role.PROFESSOR, teacher.getId()));
    }

    private boolean matches(String expected, String actual) {
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8), actual.getBytes(StandardCharsets.UTF_8));
    }

    private record Account(String username, String password, String displayName, PortalUser.Role role,
                           String teacherId) {
    }
}