package com.example.educationalqualityproject.auth;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortalAccessInterceptorTest {

    private final PortalAccessInterceptor interceptor = new PortalAccessInterceptor();

    @Test
    void linkedTeacherCanReadAndUpdateOwnAvailability() throws Exception {
        assertAllowed("/api/teachers/teacher-1/availability", "GET",
                new PortalUser("professor", "Professor", PortalUser.Role.PROFESSOR, "teacher-1"));
        assertAllowed("/api/teachers/teacher-1/availability", "POST",
                new PortalUser("professor", "Professor", PortalUser.Role.PROFESSOR, "teacher-1"));
    }

    @Test
    void teacherCannotAccessAnotherTeachersAvailability() throws Exception {
        MockHttpServletRequest request = request("/api/teachers/teacher-2/availability", "GET",
                new PortalUser("professor", "Professor", PortalUser.Role.PROFESSOR, "teacher-1"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
    }

    @Test
    void linkedTeacherCanReadAcademicPeriodsAndOwnSchedules() throws Exception {
        PortalUser teacher = new PortalUser("professor", "Professor", PortalUser.Role.PROFESSOR, "teacher-1");
        assertAllowed("/api/academic-periods", "GET", teacher);
        assertAllowed("/api/teachers/teacher-1/schedules", "GET", teacher);
    }

    @Test
    void professorCannotAccessScheduleManagementOrOtherRecipientsNotifications() throws Exception {
        PortalUser teacher = new PortalUser("professor", "Professor", PortalUser.Role.PROFESSOR, "teacher-1");
        assertDenied("/api/schedules/schedule-1/approve", "POST", teacher);
        MockHttpServletRequest request = request("/api/schedules/schedule-1/notifications", "GET", teacher);
        request.setParameter("recipient", "teacher-2");
        MockHttpServletResponse response = new MockHttpServletResponse();
        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        assertDenied("/api/schedules/schedule-2", "GET", teacher);
    }

    private void assertAllowed(String path, String method, PortalUser user) throws Exception {
        MockHttpServletRequest request = request(path, method, user);
        MockHttpServletResponse response = new MockHttpServletResponse();
        assertTrue(interceptor.preHandle(request, response, new Object()));
    }

    private void assertDenied(String path, String method, PortalUser user) throws Exception {
        MockHttpServletRequest request = request(path, method, user);
        MockHttpServletResponse response = new MockHttpServletResponse();
        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
    }

    private MockHttpServletRequest request(String path, String method, PortalUser user) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("portalUser", user);
        request.setSession(session);
        return request;
    }
}