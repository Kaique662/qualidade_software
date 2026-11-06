package com.example.performance;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PerformanceReservationStoreTest {

    @Test
    void permitsOnlyLoopbackPostgresUrls() {
        assertDoesNotThrow(() ->
                PerformanceReservationStore.requireLoopbackUrl(
                        "jdbc:postgresql://127.0.0.1:5432/perf_reservations"));
        assertDoesNotThrow(() ->
                PerformanceReservationStore.requireLoopbackUrl(
                        "jdbc:postgresql://localhost:5432/perf_reservations"));
        assertThrows(IllegalArgumentException.class, () ->
                PerformanceReservationStore.requireLoopbackUrl(
                        "jdbc:postgresql://db.example.invalid:5432/perf_reservations"));
        assertThrows(IllegalArgumentException.class, () ->
                PerformanceReservationStore.requireLoopbackUrl(
                        "jdbc:mysql://127.0.0.1:3306/perf_reservations"));
    }
}
