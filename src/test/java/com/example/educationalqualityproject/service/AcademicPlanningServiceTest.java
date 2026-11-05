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
        verify(academicScheduleRepository).save(schedule);
    }

    private AcademicPeriod period(String name, LocalDate startsOn, LocalDate endsOn) {
        AcademicPeriod period = new AcademicPeriod();
        period.setName(name);
        period.setStartsOn(startsOn);
        period.setEndsOn(endsOn);
        return period;
    }
}