package com.example.performance;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PerformanceReservationControllerTest {

    private static final Instant START = Instant.parse("2027-01-15T09:00:00Z");
    private static final Instant END = Instant.parse("2027-01-15T10:00:00Z");

    private final FakeReservationRepository store = new FakeReservationRepository();
    private final PerformanceReservationController controller =
            new PerformanceReservationController(store);

    @Test
    void createsReservationWhenPostgresAcceptsBothExclusionChecks() throws Exception {
        store.acceptReservation = true;

        var response = controller.reserve(
                new PerformanceReservationController.ReservationRequest("room-1", "teacher-1", START, END));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("ACCEPTED", response.getBody().status());
    }

    @Test
    void returnsConflictWhenPostgresExclusionConstraintRejectsInterval() throws Exception {
        store.acceptReservation = false;

        var response = controller.reserve(
                new PerformanceReservationController.ReservationRequest("room-1", "teacher-1", START, END));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("CONFLICT", response.getBody().status());
    }

    @Test
    void rejectsInvalidIntervalBeforeUsingDatabase() {
        var request = new PerformanceReservationController.ReservationRequest(
                "room-1", "teacher-1", END, START);

        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class, () -> controller.reserve(request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    private static class FakeReservationRepository implements PerformanceReservationRepository {
        private boolean acceptReservation;

        @Override
        public boolean isAvailable(String resourceId, Instant startsAt, Instant endsAt) {
            return true;
        }

        @Override
        public boolean reserve(String resourceId, String teacherId, Instant startsAt, Instant endsAt)
                throws SQLException {
            return acceptReservation;
        }
    }
}
