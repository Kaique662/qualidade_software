package com.example.educationalqualityproject.service;

import com.example.educationalqualityproject.entity.AcademicPeriod;
import com.example.educationalqualityproject.entity.AcademicSchedule;
import com.example.educationalqualityproject.entity.ClassGroup;
import com.example.educationalqualityproject.entity.Course;
import com.example.educationalqualityproject.entity.Subject;
import com.example.educationalqualityproject.entity.Teacher;
import com.example.educationalqualityproject.repository.AcademicPeriodRepository;
import com.example.educationalqualityproject.repository.AcademicScheduleRepository;
import com.example.educationalqualityproject.repository.ClassGroupRepository;
import com.example.educationalqualityproject.repository.CourseRepository;
import com.example.educationalqualityproject.repository.ManagedResourceRepository;
import com.example.educationalqualityproject.repository.ResourceBlockRepository;
import com.example.educationalqualityproject.repository.SubjectRepository;
import com.example.educationalqualityproject.repository.TeacherAvailabilityRepository;
import com.example.educationalqualityproject.repository.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.web.server.ResponseStatusException;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcademicPlanningServiceTest {

    @Mock
    private AcademicPeriodRepository academicPeriodRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private ClassGroupRepository classGroupRepository;
    @Mock
    private SubjectRepository subjectRepository;
    @Mock
    private AcademicScheduleRepository academicScheduleRepository;
    @Mock
    private TeacherRepository teacherRepository;
    @Mock
    private ManagedResourceRepository managedResourceRepository;
    @Mock
    private TeacherAvailabilityRepository teacherAvailabilityRepository;
    @Mock
    private ResourceBlockRepository resourceBlockRepository;
    private AcademicPlanningService service;

        @BeforeEach
        void setUp() {
        ResourcePlanningService resourcePlanningService = new ResourcePlanningService(
            managedResourceRepository, teacherAvailabilityRepository, resourceBlockRepository,
            academicScheduleRepository, academicPeriodRepository, teacherRepository);
        service = new AcademicPlanningService(academicPeriodRepository, courseRepository,
            classGroupRepository, subjectRepository, academicScheduleRepository,
            teacherRepository, resourcePlanningService);
        }

    @Test
    void shouldCreateAcademicPeriodWithValidDateRange() {
        AcademicPeriod period = period("2026.1", LocalDate.parse("2026-02-01"), LocalDate.parse("2026-06-30"));
        when(academicPeriodRepository.save(period)).thenReturn(period);

        AcademicPeriod saved = service.saveAcademicPeriod(null, period);

        assertEquals(period, saved);
        verify(academicPeriodRepository).save(period);
    }

    @Test
    void shouldRejectAcademicPeriodWhenEndDatePrecedesStartDate() {
        AcademicPeriod period = period("2026.1", LocalDate.parse("2026-07-01"), LocalDate.parse("2026-06-30"));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.saveAcademicPeriod(null, period));

        assertEquals(400, error.getStatusCode().value());
        verify(academicPeriodRepository, never()).save(any());
    }

    @Test
    void shouldRejectClassGroupWithUnknownCourse() {
        ClassGroup classGroup = new ClassGroup();
        classGroup.setCode("ADS-2026-1");
        classGroup.setName("ADS manhã");
        classGroup.setCourseId("missing-course");
        classGroup.setAcademicPeriodId("period-1");

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.saveClassGroup(null, classGroup));

        assertEquals(400, error.getStatusCode().value());
        verify(classGroupRepository, never()).save(any());
    }

    @Test
    void shouldSaveScheduleAsDraftWhenItsReferencesAreConsistent() {
        AcademicPeriod period = period("2026.1", LocalDate.parse("2026-02-01"), LocalDate.parse("2026-06-30"));
        period.setId("period-1");
        Course course = new Course();
        course.setId("course-1");
        ClassGroup classGroup = new ClassGroup();
        classGroup.setCourseId("course-1");
        classGroup.setAcademicPeriodId("period-1");
        Subject subject = new Subject();
        subject.setCourseId("course-1");
        AcademicSchedule schedule = new AcademicSchedule();
        schedule.setName("Grade 2026.1");
        schedule.setAcademicPeriodId("period-1");
        schedule.setStatus("PUBLICADA");
        AcademicSchedule.ScheduleVersion forgedVersion = new AcademicSchedule.ScheduleVersion();
        forgedVersion.setNumber(99);
        schedule.setVersions(List.of(forgedVersion));
        AcademicSchedule.ScheduleAuditEvent forgedEvent = new AcademicSchedule.ScheduleAuditEvent();
        forgedEvent.setAction("EVENTO_FORJADO");
        schedule.setAuditEvents(List.of(forgedEvent));
        AcademicSchedule.ScheduledActivity activity = new AcademicSchedule.ScheduledActivity();
        activity.setCourseId("course-1");
        activity.setClassId("class-1");
        activity.setSubjectId("subject-1");
        activity.setTeacherIds(List.of("teacher-1"));
        activity.setStartsAt(LocalDateTime.parse("2026-03-02T08:00:00"));
        activity.setEndsAt(LocalDateTime.parse("2026-03-02T10:00:00"));
        schedule.setActivities(List.of(activity));

        when(academicPeriodRepository.findById("period-1")).thenReturn(Optional.of(period));
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
        when(classGroupRepository.findById("class-1")).thenReturn(Optional.of(classGroup));
        when(subjectRepository.findById("subject-1")).thenReturn(Optional.of(subject));
        when(teacherRepository.findById("teacher-1")).thenReturn(Optional.of(new Teacher()));
        when(academicScheduleRepository.save(schedule)).thenReturn(schedule);

        AcademicSchedule saved = service.saveAcademicSchedule(null, schedule);

        assertEquals("RASCUNHO", saved.getStatus());
        assertEquals(1, saved.getVersionNumber());
        assertEquals(1, saved.getVersions().size());
        assertEquals("GRADE_CRIADA", saved.getAuditEvents().get(0).getAction());
        verify(academicScheduleRepository).save(schedule);
    }

    @Test
    void shouldRequireApprovalBeforePublishingAndNotifyTeachers() {
        AcademicSchedule schedule = workflowSchedule();
        when(academicScheduleRepository.findById("schedule-1")).thenReturn(Optional.of(schedule));
        when(academicScheduleRepository.save(any(AcademicSchedule.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.submitScheduleForApproval("schedule-1", "coordenadora");
        assertEquals("PENDENTE_APROVACAO", schedule.getStatus());
        service.approveSchedule("schedule-1", "diretora");
        assertEquals("APROVADA", schedule.getStatus());
        service.publishSchedule("schedule-1", "diretora");

        assertEquals("PUBLICADA", schedule.getStatus());
        assertEquals(1, schedule.getPublishedVersionNumber());
        assertTrue(schedule.getNotifications().stream()
                .anyMatch(notification -> "teacher-1".equals(notification.getRecipient())));
        assertTrue(schedule.getAuditEvents().stream()
                .anyMatch(event -> "GRADE_PUBLICADA".equals(event.getAction())
                        && "diretora".equals(event.getActor())));
    }

    @Test
    void shouldRejectPublishingDraftSchedule() {
        AcademicSchedule schedule = workflowSchedule();
        when(academicScheduleRepository.findById("schedule-1")).thenReturn(Optional.of(schedule));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.publishSchedule("schedule-1", "diretora"));

        assertEquals(409, error.getStatusCode().value());
        verify(academicScheduleRepository, never()).save(any());
    }

    @Test
    void shouldCompareIncludedRemovedAndChangedActivitiesBetweenVersions() {
        AcademicSchedule schedule = workflowSchedule();
        AcademicSchedule.ScheduleVersion previous = schedule.getVersions().get(0);
        AcademicSchedule.ScheduledActivity removed = activity("removed-1", "teacher-1");
        AcademicSchedule.ScheduledActivity changed = activity("changed-1", "teacher-1");
        previous.setActivities(List.of(removed, changed));
        AcademicSchedule.ScheduleVersion next = new AcademicSchedule.ScheduleVersion();
        next.setNumber(2);
        AcademicSchedule.ScheduledActivity changedCopy = activity("changed-1", "teacher-2");
        AcademicSchedule.ScheduledActivity included = activity("included-1", "teacher-1");
        next.setActivities(List.of(changedCopy, included));
        schedule.getVersions().add(next);
        when(academicScheduleRepository.findById("schedule-1")).thenReturn(Optional.of(schedule));

        List<AcademicSchedule.ScheduleChange> changes =
                service.compareScheduleVersions("schedule-1", 1, 2);

        assertEquals(3, changes.size());
        assertTrue(changes.stream().anyMatch(change -> "REMOVIDA".equals(change.getType())));
        assertTrue(changes.stream().anyMatch(change -> "ALTERADA".equals(change.getType())));
        assertTrue(changes.stream().anyMatch(change -> "INCLUIDA".equals(change.getType())));
    }

    @Test
    void shouldRejectScheduleUpdateWhenExpectedVersionIsStale() {
        AcademicSchedule existing = workflowSchedule();
        existing.setVersionNumber(3);
        AcademicSchedule update = new AcademicSchedule();
        update.setName("Grade atualizada");
        update.setAcademicPeriodId("period-1");
        update.setActivities(List.of());
        AcademicPeriod period = period("2026.1", LocalDate.parse("2026-02-01"), LocalDate.parse("2026-06-30"));
        period.setId("period-1");
        when(academicScheduleRepository.findById("schedule-1")).thenReturn(Optional.of(existing));
        when(academicPeriodRepository.findById("period-1")).thenReturn(Optional.of(period));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.saveAcademicSchedule("schedule-1", update, "coordenadora", 2));

        assertEquals(409, error.getStatusCode().value());
        verify(academicScheduleRepository, never()).save(any());
    }

        @Test
        void shouldTrackAdjustmentRequestAndNotifyRequesterOfDecision() {
        AcademicSchedule schedule = workflowSchedule();
        when(academicScheduleRepository.findById("schedule-1")).thenReturn(Optional.of(schedule));
        when(academicScheduleRepository.save(any(AcademicSchedule.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        AcademicSchedule.AdjustmentRequest request = service.requestScheduleAdjustment(
            "schedule-1", "teacher-1", "activity-1", "Conflito com horário informado");
        assertEquals("ABERTA", request.getStatus());
        AcademicSchedule.AdjustmentRequest responded = service.respondToAdjustment(
            "schedule-1", request.getId(), "coordenadora", "ACEITA", "Horário será revisado");
        assertEquals("ACEITA", responded.getStatus());
        assertEquals("coordenadora", responded.getRespondedBy());

        List<AcademicSchedule.ScheduleNotification> notifications =
            service.getScheduleNotifications("schedule-1", "teacher-1");
        assertEquals(1, notifications.size());
        service.markNotificationRead("schedule-1", notifications.get(0).getId(), "teacher-1");

        assertTrue(notifications.get(0).isRead());
        assertTrue(schedule.getAuditEvents().stream()
            .anyMatch(event -> "AJUSTE_ACEITA".equals(event.getAction())));
        }

    private AcademicSchedule workflowSchedule() {
        AcademicSchedule schedule = new AcademicSchedule();
        schedule.setId("schedule-1");
        schedule.setName("Grade 2026.1");
        schedule.setAcademicPeriodId("period-1");
        schedule.setVersionNumber(1);
        AcademicSchedule.ScheduledActivity activity = activity("activity-1", "teacher-1");
        schedule.setActivities(List.of(activity));
        AcademicSchedule.ScheduleVersion version = new AcademicSchedule.ScheduleVersion();
        version.setNumber(1);
        version.setCreatedBy("coordenadora");
        version.setActivities(List.of(activity));
        schedule.setVersions(new java.util.ArrayList<>(List.of(version)));
        return schedule;
    }

    private AcademicSchedule.ScheduledActivity activity(String id, String teacherId) {
        AcademicSchedule.ScheduledActivity activity = new AcademicSchedule.ScheduledActivity();
        activity.setId(id);
        activity.setTeacherIds(List.of(teacherId));
        return activity;
    }

    private AcademicPeriod period(String name, LocalDate startsOn, LocalDate endsOn) {
        AcademicPeriod period = new AcademicPeriod();
        period.setName(name);
        period.setStartsOn(startsOn);
        period.setEndsOn(endsOn);
        return period;
    }
}