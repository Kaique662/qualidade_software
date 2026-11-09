package com.example.performance;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PerformanceAuthServiceTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void authenticatesCsvUserAndIssuesStableOpaqueToken() throws Exception {
        PerformanceAuthService service = authService(
                "username,password,resourceId,teacherId\n"
                        + "tester@example.invalid,\"a,password\",room-1,teacher-1\n");

        String firstToken = service.authenticate("tester@example.invalid", "a,password");
        String secondToken = service.authenticate("tester@example.invalid", "a,password");

        assertNotNull(firstToken);
        assertNotEquals("a,password", firstToken);
        assertTrue(service.isValidToken(firstToken));
        org.junit.jupiter.api.Assertions.assertEquals(firstToken, secondToken);
    }

    @Test
    void rejectsUnknownUserAndWrongPassword() throws Exception {
        PerformanceAuthService service = authService(
                "username,password,resourceId,teacherId\n"
                        + "tester@example.invalid,local-test-password,room-1,teacher-1\n");

        assertNull(service.authenticate("missing@example.invalid", "local-test-password"));
        assertNull(service.authenticate("tester@example.invalid", "wrong-password"));
        assertFalse(service.isValidToken("not-a-token"));
    }

    @Test
    void rejectsDuplicateUsersInCsv() throws Exception {
        Path users = writeUsers(
                "username,password,resourceId,teacherId\n"
                        + "tester@example.invalid,one,room-1,teacher-1\n"
                        + "tester@example.invalid,two,room-1,teacher-1\n");

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class, () -> new PerformanceAuthService(users.toString()));
    }

    private PerformanceAuthService authService(String contents) throws Exception {
        return new PerformanceAuthService(writeUsers(contents).toString());
    }

    private Path writeUsers(String contents) throws Exception {
        Path users = temporaryDirectory.resolve("users.csv");
        Files.writeString(users, contents);
        return users;
    }
}
