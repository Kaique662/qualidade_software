package com.example.educationalqualityproject.controller;

import com.example.educationalqualityproject.auth.PortalUser;
import com.example.educationalqualityproject.service.TeacherService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TeacherAccountControllerTest {

    @Test
    void changesPasswordForTheTeacherLinkedToTheSession() {
        FakeTeacherService teacherService = new FakeTeacherService();
        teacherService.passwordChangeResult = true;
        TeacherAccountController controller = new TeacherAccountController(teacherService);

        String view = controller.changePassword(
                new PortalUser("ada@example.test", "Ada", PortalUser.Role.PROFESSOR, "teacher-1"),
                "old-secret", "new-secret", "new-secret");

        assertEquals("redirect:/professor?passwordChanged", view);
        assertEquals("teacher-1", teacherService.teacherId);
        assertEquals("old-secret", teacherService.currentPassword);
        assertEquals("new-secret", teacherService.newPassword);
    }

    @Test
    void rejectsMismatchedConfirmationAndWeakPasswords() {
        FakeTeacherService teacherService = new FakeTeacherService();
        TeacherAccountController controller = new TeacherAccountController(teacherService);
        PortalUser user = new PortalUser("ada@example.test", "Ada", PortalUser.Role.PROFESSOR, "teacher-1");

        assertEquals("redirect:/professor?passwordError=mismatch",
                controller.changePassword(user, "old-secret", "new-secret", "different"));
        assertEquals("redirect:/professor?passwordError=weak",
                controller.changePassword(user, "old-secret", "short", "short"));
        assertFalse(teacherService.passwordChangeCalled);
    }

    private static class FakeTeacherService extends TeacherService {
        private boolean passwordChangeResult;
        private boolean passwordChangeCalled;
        private String teacherId;
        private String currentPassword;
        private String newPassword;

        @Override
        public boolean changePassword(String id, String current, String updated) {
            passwordChangeCalled = true;
            teacherId = id;
            currentPassword = current;
            newPassword = updated;
            return passwordChangeResult;
        }
    }
}
