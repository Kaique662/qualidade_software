package com.example.educationalqualityproject.controller;

import com.example.educationalqualityproject.auth.PortalUser;
import com.example.educationalqualityproject.entity.Student;
import com.example.educationalqualityproject.entity.Teacher;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.ui.ExtendedModelMap;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DirectoryTemplateRenderingTest {

    @Test
    void studentAndTeacherDirectoriesRenderForAdmin() {
        TemplateEngine templateEngine = templateEngine();
        PortalUser admin = new PortalUser("admin", "Administrador", PortalUser.Role.ADMIN);

        WebContext studentContext = context(admin, "students", "/students");
        studentContext.setVariable("students", List.of(
                new Student("Ada Lovelace", "ada@example.test", "S-1"),
                new Student("", "missing-name@example.test", "S-2")));
        String students = templateEngine.process("student/list", studentContext);

        WebContext teacherContext = context(admin, "teachers", "/teachers");
        teacherContext.setVariable("teachers", List.of(
                new Teacher("Grace Hopper", "grace@example.test", "Computação"),
                new Teacher("", "missing-name@example.test", "Computação")));
        String teachers = templateEngine.process("teacher/list", teacherContext);

        assertTrue(students.contains("Ada Lovelace"));
        assertTrue(students.contains("Nome não informado"));
        assertTrue(teachers.contains("Grace Hopper"));
        assertTrue(teachers.contains("Nome não informado"));
    }

    @Test
    void teacherFormOffersPasswordForNewAccounts() {
        WebContext context = context(
                new PortalUser("admin", "Administrador", PortalUser.Role.ADMIN), "teachers", "/teachers/new");
        context.setVariable("teacher", new Teacher());
        String form = templateEngine().process("teacher/form", context);

        assertTrue(form.contains("name=\"password\""));
        assertTrue(form.contains("Mínimo de 8 caracteres"));
    }

    @Test
    void mongoFailureUsesRecoverableUnavailablePage() {
        DirectoryDataAccessExceptionHandler handler = new DirectoryDataAccessExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/teachers");
        ExtendedModelMap model = new ExtendedModelMap();

        String view = handler.handleMongoUnavailable(
                new DataAccessResourceFailureException("MongoDB unavailable"), request, model);

        assertTrue("error/directory-unavailable".equals(view));
        assertTrue("professores".equals(model.get("directoryName")));
        assertTrue("/teachers".equals(model.get("retryPath")));
    }

    private TemplateEngine templateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }

    private WebContext context(PortalUser user, String activePage, String path) {
        MockServletContext servletContext = new MockServletContext();
        JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(servletContext);
        MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
        request.setRequestURI(path);
        MockHttpServletResponse response = new MockHttpServletResponse();
        WebContext context = new WebContext(application.buildExchange(request, response),
                Locale.forLanguageTag("pt-BR"));
        context.setVariable("portalUser", user);
        context.setVariable("activePage", activePage);
        return context;
    }
}