package com.example.educationalqualityproject.service;

import com.example.educationalqualityproject.entity.AcademicSchedule;
import com.example.educationalqualityproject.entity.ManagedResource;
import com.example.educationalqualityproject.entity.ResourceAllocationRequest;
import com.example.educationalqualityproject.entity.ResourceBlock;
import com.example.educationalqualityproject.entity.TeacherAvailability;
import com.example.educationalqualityproject.repository.AcademicPeriodRepository;
import com.example.educationalqualityproject.repository.AcademicScheduleRepository;
import com.example.educationalqualityproject.repository.ManagedResourceRepository;
import com.example.educationalqualityproject.repository.ResourceBlockRepository;
import com.example.educationalqualityproject.repository.TeacherAvailabilityRepository;
import com.example.educationalqualityproject.repository.TeacherRepository;
import com.example.educationalqualityproject.reservation.ReservationStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.web.server.ResponseStatusException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourcePlanningServiceTest {

    @Mock
    private ManagedResourceRepository resourceRepository;
    @Mock
    private TeacherAvailabilityRepository availabilityRepository;
    @Mock
    private ResourceBlockRepository blockRepository;
    @Mock
    private AcademicScheduleRepository scheduleRepository;
    @Mock
    private AcademicPeriodRepository periodRepository;
    @Mock
    private TeacherRepository teacherRepository;
        @Mock
        private ReservationStore reservationStore;

    @InjectMocks
    private ResourcePlanningService service;

    @Test
    void shouldIncludeSqlReservationsInAvailableMaterialQuantity() {
        ManagedResource projector = material("projector-1", 5);
        service.setReservationStore(reservationStore);
        when(resourceRepository.findById("projector-1")).thenReturn(Optional.of(projector));
        when(scheduleRepository.findAll()).thenReturn(List.of());
        when(reservationStore.findActiveMaterialReservations(eq("projector-1"), any(Instant.class),
                any(Instant.class))).thenReturn(List.of(new ReservationStore.MaterialInterval(
                        Instant.parse("2026-03-02T10:15:00Z"), Instant.parse("2026-03-02T10:30:00Z"), 2)));

        int available = service.getAvailableMaterialQuantity("projector-1",
                LocalDateTime.parse("2026-03-02T10:00:00"), LocalDateTime.parse("2026-03-02T11:00:00"));

        assertEquals(3, available);
    }

        @Test
        void shouldFilterResourceBlocksByResourceId() {
                ResourceBlock matching = new ResourceBlock();
                matching.setId("block-1");
                matching.setResourceId("room-1");
                ResourceBlock other = new ResourceBlock();
                other.setId("block-2");
                other.setResourceId("projector-1");
                when(blockRepository.findAll()).thenReturn(List.of(matching, other));

                List<ResourceBlock> result = service.getResourceBlocks("room-1");

                assertEquals(1, result.size());
                assertEquals("block-1", result.get(0).getId());
        }

    @Test
    void shouldAllocateRoomAndMaterialsToScheduleActivity() {
        AcademicSchedule schedule = schedule("schedule-1", activity("activity-1", "teacher-1",
                "2026-03-02T10:00:00", "2026-03-02T11:00:00"));
        ManagedResource room = room("room-1", 30);
        ManagedResource material = material("projector-1", 2);
        ResourceAllocationRequest request = new ResourceAllocationRequest();
        request.setRoomResourceId("room-1");
        AcademicSchedule.MaterialAssignment assignment = new AcademicSchedule.MaterialAssignment();
        assignment.setResourceId("projector-1");
        assignment.setQuantity(2);
        request.setMaterials(List.of(assignment));

        when(scheduleRepository.findById("schedule-1")).thenReturn(Optional.of(schedule));
        when(scheduleRepository.findAll()).thenReturn(List.of(schedule));
        when(availabilityRepository.findAll()).thenReturn(List.of());
        when(resourceRepository.findById("room-1")).thenReturn(Optional.of(room));
        when(resourceRepository.findById("projector-1")).thenReturn(Optional.of(material));
        when(blockRepository.findAll()).thenReturn(List.of());
        when(scheduleRepository.save(schedule)).thenReturn(schedule);

        AcademicSchedule result = service.allocateActivity("schedule-1", "activity-1", request);

        assertEquals("room-1", result.getActivities().get(0).getRoomResourceId());
        assertEquals(2, result.getActivities().get(0).getMaterials().get(0).getQuantity());
        assertEquals(2, result.getVersionNumber());
        assertEquals(2, result.getVersions().size());
        verify(scheduleRepository).save(schedule);
    }

    @Test
    void shouldRejectRoomAlreadyAllocatedDuringOverlappingInterval() {
        AcademicSchedule current = schedule("schedule-1", activity("activity-1", "teacher-1",
                "2026-03-02T10:00:00", "2026-03-02T11:00:00"));
        AcademicSchedule occupied = schedule("schedule-2", activity("activity-2", "teacher-2",
                "2026-03-02T10:30:00", "2026-03-02T11:30:00"));
        occupied.getActivities().get(0).setRoomResourceId("room-1");
        ResourceAllocationRequest request = new ResourceAllocationRequest();
        request.setRoomResourceId("room-1");

        when(scheduleRepository.findById("schedule-1")).thenReturn(Optional.of(current));
        when(scheduleRepository.findAll()).thenReturn(List.of(current, occupied));
        when(availabilityRepository.findAll()).thenReturn(List.of());
        when(resourceRepository.findById("room-1")).thenReturn(Optional.of(room("room-1", 30)));
        when(blockRepository.findAll()).thenReturn(List.of());

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.allocateActivity("schedule-1", "activity-1", request));

        assertEquals(409, error.getStatusCode().value());
        verify(scheduleRepository, never()).save(any());
    }

        @Test
        void shouldRejectAllocationWhenResourceHasConfirmedBlock() {
                AcademicSchedule schedule = schedule("schedule-1", activity("activity-1", "teacher-1",
                                "2026-03-02T10:00:00", "2026-03-02T11:00:00"));
                ResourceBlock block = new ResourceBlock();
                block.setResourceId("room-1");
                block.setStatus(ResourceBlock.BlockStatus.CONFIRMED);
                block.setStartsAt(LocalDateTime.parse("2026-03-02T10:30:00"));
                block.setEndsAt(LocalDateTime.parse("2026-03-02T12:00:00"));
                ResourceAllocationRequest request = new ResourceAllocationRequest();
                request.setRoomResourceId("room-1");

                when(scheduleRepository.findById("schedule-1")).thenReturn(Optional.of(schedule));
                when(scheduleRepository.findAll()).thenReturn(List.of(schedule));
                when(availabilityRepository.findAll()).thenReturn(List.of());
                when(resourceRepository.findById("room-1")).thenReturn(Optional.of(room("room-1", 30)));
                when(blockRepository.findAll()).thenReturn(List.of(block));

                ResponseStatusException error = assertThrows(ResponseStatusException.class,
                                () -> service.allocateActivity("schedule-1", "activity-1", request));

                assertEquals(409, error.getStatusCode().value());
                verify(scheduleRepository, never()).save(any());
        }

    @Test
    void shouldRejectAllocationDuringMandatoryRecurringTeacherUnavailability() {
        AcademicSchedule schedule = schedule("schedule-1", activity("activity-1", "teacher-1",
                "2026-03-02T10:00:00", "2026-03-02T11:00:00"));
        TeacherAvailability unavailable = new TeacherAvailability();
        unavailable.setTeacherId("teacher-1");
        unavailable.setAcademicPeriodId("period-1");
        unavailable.setType(TeacherAvailability.AvailabilityType.UNAVAILABLE);
        unavailable.setConstraintKind(TeacherAvailability.ConstraintKind.MANDATORY);
        unavailable.setRecurringWeekly(true);
        unavailable.setValidFrom(LocalDate.parse("2026-02-01"));
        unavailable.setValidUntil(LocalDate.parse("2026-06-30"));
        unavailable.setDailyStartsAt(LocalTime.parse("09:00"));
        unavailable.setDailyEndsAt(LocalTime.parse("11:00"));
        unavailable.setWeekdays(List.of(DayOfWeek.MONDAY));
        ResourceAllocationRequest request = new ResourceAllocationRequest();
        request.setRoomResourceId("room-1");

        when(scheduleRepository.findById("schedule-1")).thenReturn(Optional.of(schedule));
        when(scheduleRepository.findAll()).thenReturn(List.of(schedule));
        when(availabilityRepository.findAll()).thenReturn(List.of(unavailable));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.allocateActivity("schedule-1", "activity-1", request));

        assertEquals(409, error.getStatusCode().value());
        verify(resourceRepository, never()).findById(any());
    }

    @Test
    void shouldRejectMaterialAllocationWhenConcurrentQuantityExceedsStock() {
        AcademicSchedule current = schedule("schedule-1", activity("activity-1", "teacher-1",
                "2026-03-02T10:00:00", "2026-03-02T11:00:00"));
        AcademicSchedule occupied = schedule("schedule-2", activity("activity-2", "teacher-2",
                "2026-03-02T10:30:00", "2026-03-02T11:30:00"));
        AcademicSchedule.MaterialAssignment occupiedMaterial = new AcademicSchedule.MaterialAssignment();
        occupiedMaterial.setResourceId("projector-1");
        occupiedMaterial.setQuantity(3);
        occupied.getActivities().get(0).setMaterials(List.of(occupiedMaterial));
        ResourceAllocationRequest request = new ResourceAllocationRequest();
        request.setRoomResourceId("room-1");
        AcademicSchedule.MaterialAssignment requestedMaterial = new AcademicSchedule.MaterialAssignment();
        requestedMaterial.setResourceId("projector-1");
        requestedMaterial.setQuantity(2);
        request.setMaterials(List.of(requestedMaterial));

        when(scheduleRepository.findById("schedule-1")).thenReturn(Optional.of(current));
        when(scheduleRepository.findAll()).thenReturn(List.of(current, occupied));
        when(availabilityRepository.findAll()).thenReturn(List.of());
        when(resourceRepository.findById("room-1")).thenReturn(Optional.of(room("room-1", 30)));
        when(resourceRepository.findById("projector-1")).thenReturn(Optional.of(material("projector-1", 4)));
        when(blockRepository.findAll()).thenReturn(List.of());

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.allocateActivity("schedule-1", "activity-1", request));

        assertEquals(409, error.getStatusCode().value());
        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void shouldCreatePendingBlockWithImpactedAllocations() {
        AcademicSchedule occupied = schedule("schedule-2", activity("activity-2", "teacher-2",
                "2026-03-02T10:00:00", "2026-03-02T11:00:00"));
        occupied.getActivities().get(0).setRoomResourceId("room-1");
        ResourceBlock block = new ResourceBlock();
        block.setResourceId("room-1");
        block.setReason("Manutenção preventiva");
        block.setResponsible("gestora");
        block.setStartsAt(LocalDateTime.parse("2026-03-02T10:30:00"));
        block.setEndsAt(LocalDateTime.parse("2026-03-02T12:00:00"));

        when(resourceRepository.findById("room-1")).thenReturn(Optional.of(room("room-1", 30)));
        when(scheduleRepository.findAll()).thenReturn(List.of(occupied));
        when(blockRepository.save(block)).thenReturn(block);

        ResourceBlock saved = service.createBlock(block);

        assertEquals(ResourceBlock.BlockStatus.PENDING, saved.getStatus());
        assertEquals(List.of("schedule-2/activity-2"), saved.getAffectedAllocationIds());
        assertEquals("CREATED", saved.getHistory().get(0).getAction());
    }

        @Test
        void shouldReportAvailableMaterialQuantityForRequestedInterval() {
                AcademicSchedule occupied = schedule("schedule-2", activity("activity-2", "teacher-2",
                                "2026-03-02T10:30:00", "2026-03-02T11:30:00"));
                AcademicSchedule.MaterialAssignment reserved = new AcademicSchedule.MaterialAssignment();
                reserved.setResourceId("projector-1");
                reserved.setQuantity(2);
                occupied.getActivities().get(0).setMaterials(List.of(reserved));
                ManagedResource projector = material("projector-1", 5);
                when(resourceRepository.findAll()).thenReturn(List.of(projector));
                when(scheduleRepository.findAll()).thenReturn(List.of(occupied));
                when(blockRepository.findAll()).thenReturn(List.of());

                List<ManagedResource> result = service.searchResources(null, null,
                                ManagedResource.ResourceType.MATERIAL, null, ManagedResource.ResourceState.AVAILABLE,
                                LocalDateTime.parse("2026-03-02T10:00:00"), LocalDateTime.parse("2026-03-02T11:00:00"));

                assertEquals(1, result.size());
                assertEquals(3, result.get(0).getAvailableQuantity());
        }

    private AcademicSchedule schedule(String id, AcademicSchedule.ScheduledActivity activity) {
        AcademicSchedule schedule = new AcademicSchedule();
        schedule.setId(id);
        schedule.setAcademicPeriodId("period-1");
        schedule.setActivities(new ArrayList<>(List.of(activity)));
        return schedule;
    }

    private AcademicSchedule.ScheduledActivity activity(String id, String teacherId,
                                                          String startsAt, String endsAt) {
        AcademicSchedule.ScheduledActivity activity = new AcademicSchedule.ScheduledActivity();
        activity.setId(id);
        activity.setTeacherIds(List.of(teacherId));
        activity.setStartsAt(LocalDateTime.parse(startsAt));
        activity.setEndsAt(LocalDateTime.parse(endsAt));
        return activity;
    }

    private ManagedResource room(String id, int capacity) {
        ManagedResource room = new ManagedResource();
        room.setId(id);
        room.setType(ManagedResource.ResourceType.ROOM);
        room.setState(ManagedResource.ResourceState.AVAILABLE);
        room.setCapacity(capacity);
        room.setFeatures(List.of());
        return room;
    }

    private ManagedResource material(String id, int totalQuantity) {
        ManagedResource material = new ManagedResource();
        material.setId(id);
        material.setType(ManagedResource.ResourceType.MATERIAL);
        material.setState(ManagedResource.ResourceState.AVAILABLE);
        material.setTotalQuantity(totalQuantity);
        return material;
    }
}