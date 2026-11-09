package com.example.performance;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.time.Instant;

@RestController
@RequestMapping("/api")
public class PerformanceReservationController {

    private final PerformanceReservationRepository store;

    public PerformanceReservationController(PerformanceReservationRepository store) {
        this.store = store;
    }

    @GetMapping("/resources/{resourceId}/availability")
    public AvailabilityResponse availability(
            @PathVariable String resourceId,
            @RequestParam Instant startsAt,
            @RequestParam Instant endsAt) throws SQLException {
        validateInterval(startsAt, endsAt);
        return new AvailabilityResponse(
                resourceId, startsAt, endsAt, store.isAvailable(resourceId, startsAt, endsAt));
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> reserve(@RequestBody ReservationRequest request)
            throws SQLException {
        if (request == null || isBlank(request.resourceId()) || isBlank(request.teacherId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resource and teacher are required");
        }
        validateInterval(request.startsAt(), request.endsAt());
        boolean accepted = store.reserve(
                request.resourceId(), request.teacherId(), request.startsAt(), request.endsAt());
        ReservationResponse body = new ReservationResponse(
                request.resourceId(), request.teacherId(), request.startsAt(), request.endsAt(),
                accepted ? "ACCEPTED" : "CONFLICT");
        return ResponseEntity.status(accepted ? HttpStatus.CREATED : HttpStatus.CONFLICT).body(body);
    }

    private static void validateInterval(Instant startsAt, Instant endsAt) {
        if (startsAt == null || endsAt == null || !startsAt.isBefore(endsAt)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A valid time interval is required");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record AvailabilityResponse(
            String resourceId, Instant startsAt, Instant endsAt, boolean available) {
    }

    public record ReservationRequest(
            String resourceId, String teacherId, Instant startsAt, Instant endsAt) {
    }

    public record ReservationResponse(
            String resourceId, String teacherId, Instant startsAt, Instant endsAt, String status) {
    }
}
