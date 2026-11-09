package com.example.educationalqualityproject.reservation;

import com.example.educationalqualityproject.entity.ManagedResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationWorkflowServiceTest {

    private static final Instant START = Instant.parse("2026-03-01T10:00:00Z");
    private static final Instant END = Instant.parse("2026-03-01T11:00:00Z");

    @Mock
        private ReservationStore repository;
    @Mock
        private ReservationResourceValidator resourcePlanningService;
    private ReservationWorkflowService service;

    @BeforeEach
    void setUp() {
        service = new ReservationWorkflowService(repository,
                new ReservationService(Clock.fixed(START, ZoneOffset.UTC)),
                resourcePlanningService);
    }

    @Test
        void shouldPassScheduleIntervalsToTransactionalMaterialCapacityCheck() {
        ReservationRequest request = new ReservationRequest("material-1",
                ReservationRequest.ResourceType.MATERIAL, "user-1", null, START, END, 2);
        ManagedResource material = material("material-1", 5, true);
        material.setPreparationBufferMinutes(5);
        material.setCleanupBufferMinutes(10);
        when(resourcePlanningService.validateReservationResource(eq("material-1"), any(), any(), eq(2)))
                .thenReturn(material);
        List<ReservationStore.MaterialInterval> scheduleReservations = List.of(
                new ReservationStore.MaterialInterval(Instant.parse("2026-03-01T10:15:00Z"),
                        Instant.parse("2026-03-01T10:30:00Z"), 2));
        when(resourcePlanningService.getMaterialReservations(eq("material-1"), any(), any()))
                .thenReturn(scheduleReservations);
        when(repository.create(eq(request), eq(true), eq(2),
                eq(Instant.parse("2026-03-01T09:55:00Z")),
                eq(Instant.parse("2026-03-01T11:10:00Z")), eq(5), eq(scheduleReservations)))
                .thenReturn(reservation(1, request, true, ReservationService.Status.SOLICITADA));

        ReservationJdbcRepository.ReservationData result = service.request(request);

        assertEquals(ReservationService.Status.SOLICITADA, result.status());
        verify(resourcePlanningService).validateReservationResource(eq("material-1"), any(), any(), eq(2));
        verify(repository).create(eq(request), eq(true), eq(2), any(), any(), eq(5), eq(scheduleReservations));
    }

    @Test
    void shouldRecordRejectedAttemptWhenDatabaseReportsReservationConflict() {
        ReservationRequest request = new ReservationRequest("room-1",
                ReservationRequest.ResourceType.ROOM, "user-1", null, START, END, 1);
        DataIntegrityViolationException databaseConflict = new DataIntegrityViolationException(
                "SQL conflict", new SQLException("Exclusion constraint", "23P01"));
        when(resourcePlanningService.validateReservationResource(eq("room-1"), any(), any(), eq(1)))
                .thenReturn(room("room-1", true));
        when(repository.create(eq(request), eq(true), eq(1), any(), any(), eq(0), eq(List.of())))
                .thenThrow(databaseConflict);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.request(request));

        assertEquals(409, error.getStatusCode().value());
        verify(repository).recordRejectedAttempt("room-1", "user-1",
                "Conflito de recurso ou capacidade de material");
    }

    @Test
    void shouldPersistApprovalOnlyForDifferentResponsibleOnRestrictedResource() {
        ReservationRequest request = new ReservationRequest("room-1",
                ReservationRequest.ResourceType.ROOM, "user-1", null, START, END, 1);
        ReservationJdbcRepository.ReservationData requested = reservation(
                7, request, true, ReservationService.Status.SOLICITADA);
        ReservationJdbcRepository.ReservationData approved = reservation(
                7, request, true, ReservationService.Status.APROVADA);
        when(repository.findById(7)).thenReturn(Optional.of(requested));
        when(repository.transition(7, ReservationService.Status.SOLICITADA,
                ReservationService.Status.APROVADA, "user-2")).thenReturn(approved);

        ReservationJdbcRepository.ReservationData result = service.approve(
                7, "user-2", ReservationService.Role.RESPONSAVEL);

        assertEquals(ReservationService.Status.APROVADA, result.status());
    }

    @Test
    void shouldRejectRestrictedSelfApprovalWithoutUpdatingDatabase() {
        ReservationRequest request = new ReservationRequest("room-1",
                ReservationRequest.ResourceType.ROOM, "user-1", null, START, END, 1);
        when(repository.findById(7)).thenReturn(Optional.of(reservation(
                7, request, true, ReservationService.Status.SOLICITADA)));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.approve(7, "user-1", ReservationService.Role.RESPONSAVEL));

        assertEquals(403, error.getStatusCode().value());
        verify(repository).recordRejectedAttempt("room-1", "user-1",
                "Aprovação sem papel permitido ou autoaprovação restrita");
    }

    private ManagedResource material(String id, int quantity, boolean restricted) {
        ManagedResource resource = new ManagedResource();
        resource.setId(id);
        resource.setType(ManagedResource.ResourceType.MATERIAL);
        resource.setTotalQuantity(quantity);
        resource.setRestricted(restricted);
        resource.setState(ManagedResource.ResourceState.AVAILABLE);
        return resource;
    }

    private ManagedResource room(String id, boolean restricted) {
        ManagedResource resource = new ManagedResource();
        resource.setId(id);
        resource.setType(ManagedResource.ResourceType.ROOM);
        resource.setCapacity(20);
        resource.setRestricted(restricted);
        resource.setState(ManagedResource.ResourceState.AVAILABLE);
        return resource;
    }

    private ReservationJdbcRepository.ReservationData reservation(long id, ReservationRequest request,
                                                                   boolean restricted,
                                                                   ReservationService.Status status) {
        return new ReservationJdbcRepository.ReservationData(id, request.resourceType(), request.resourceId(),
                request.requesterId(), ReservationService.Role.SOLICITANTE, restricted, status,
                START, END, request.quantity(), START, null, null);
    }
}