package com.example.educationalqualityproject.controller.api;

import com.example.educationalqualityproject.reservation.ReservationJdbcRepository;
import com.example.educationalqualityproject.reservation.ReservationRequest;
import com.example.educationalqualityproject.reservation.ReservationService;
import com.example.educationalqualityproject.reservation.ReservationWorkflowService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@ConditionalOnProperty(prefix = "reservations.datasource", name = "url")
public class ReservationApiController {

    private final ReservationWorkflowService reservationService;

    public ReservationApiController(ReservationWorkflowService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationJdbcRepository.ReservationData> create(
            @RequestBody ReservationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.request(request));
    }

    @GetMapping
    public List<ReservationJdbcRepository.ReservationData> list(
            @RequestParam(required = false) String resourceId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Instant startsBefore,
            @RequestParam(required = false) Instant endsAfter) {
        return reservationService.findAll(resourceId, status, startsBefore, endsAfter);
    }

    @GetMapping("/{id}")
    public ReservationJdbcRepository.ReservationData get(@PathVariable long id) {
        return reservationService.find(id);
    }

    @PostMapping("/{id}/approve")
    public ReservationJdbcRepository.ReservationData approve(
            @PathVariable long id, @RequestParam String actorId, @RequestParam ReservationService.Role role) {
        return reservationService.approve(id, actorId, role);
    }

    @PostMapping("/{id}/start")
    public ReservationJdbcRepository.ReservationData start(@PathVariable long id, @RequestParam String actorId) {
        return reservationService.startUse(id, actorId);
    }

    @PostMapping("/{id}/complete")
    public ReservationJdbcRepository.ReservationData complete(@PathVariable long id, @RequestParam String actorId) {
        return reservationService.complete(id, actorId);
    }

    @GetMapping("/{id}/history")
    public List<ReservationJdbcRepository.AuditEvent> history(@PathVariable long id) {
        return reservationService.history(id);
    }
}