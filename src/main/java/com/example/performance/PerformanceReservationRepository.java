package com.example.performance;

import java.sql.SQLException;
import java.time.Instant;

public interface PerformanceReservationRepository {

    boolean isAvailable(String resourceId, Instant startsAt, Instant endsAt) throws SQLException;

    boolean reserve(String resourceId, String teacherId, Instant startsAt, Instant endsAt)
            throws SQLException;
}
