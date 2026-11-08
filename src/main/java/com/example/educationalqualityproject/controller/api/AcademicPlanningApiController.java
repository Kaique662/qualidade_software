package com.example.educationalqualityproject.controller.api;

import com.example.educationalqualityproject.entity.AcademicPeriod;
import com.example.educationalqualityproject.entity.AcademicSchedule;
import com.example.educationalqualityproject.entity.ClassGroup;
import com.example.educationalqualityproject.entity.Course;
import com.example.educationalqualityproject.entity.Subject;
import com.example.educationalqualityproject.entity.TeacherScheduleView;
import com.example.educationalqualityproject.auth.PortalUser;
import com.example.educationalqualityproject.service.AcademicPlanningService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AcademicPlanningApiController {

    private final AcademicPlanningService academicPlanningService;

    public AcademicPlanningApiController(AcademicPlanningService academicPlanningService) {
        this.academicPlanningService = academicPlanningService;
    }

    @GetMapping("/academic-periods")
    public List<AcademicPeriod> getAcademicPeriods() {
        return academicPlanningService.getAcademicPeriods();
    }

    @GetMapping("/academic-periods/{id}")
    public AcademicPeriod getAcademicPeriod(@PathVariable String id) {
        return academicPlanningService.getAcademicPeriod(id);
    }

    @PostMapping("/academic-periods")
    public ResponseEntity<AcademicPeriod> createAcademicPeriod(@RequestBody AcademicPeriod period) {
        return ResponseEntity.status(HttpStatus.CREATED).body(academicPlanningService.saveAcademicPeriod(null, period));
    }

    @PutMapping("/academic-periods/{id}")
    public AcademicPeriod updateAcademicPeriod(@PathVariable String id, @RequestBody AcademicPeriod period) {
        return academicPlanningService.saveAcademicPeriod(id, period);
    }

    @DeleteMapping("/academic-periods/{id}")
    public ResponseEntity<Void> deleteAcademicPeriod(@PathVariable String id) {
        academicPlanningService.deleteAcademicPeriod(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/courses")
    public List<Course> getCourses() {
        return academicPlanningService.getCourses();
    }

    @GetMapping("/courses/{id}")
    public Course getCourse(@PathVariable String id) {
        return academicPlanningService.getCourse(id);
    }

    @PostMapping("/courses")
    public ResponseEntity<Course> createCourse(@RequestBody Course course) {
        return ResponseEntity.status(HttpStatus.CREATED).body(academicPlanningService.saveCourse(null, course));
    }

    @PutMapping("/courses/{id}")
    public Course updateCourse(@PathVariable String id, @RequestBody Course course) {
        return academicPlanningService.saveCourse(id, course);
    }

    @DeleteMapping("/courses/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable String id) {
        academicPlanningService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/classes")
    public List<ClassGroup> getClassGroups() {
        return academicPlanningService.getClassGroups();
    }

    @GetMapping("/classes/{id}")
    public ClassGroup getClassGroup(@PathVariable String id) {
        return academicPlanningService.getClassGroup(id);
    }

    @PostMapping("/classes")
    public ResponseEntity<ClassGroup> createClassGroup(@RequestBody ClassGroup classGroup) {
        return ResponseEntity.status(HttpStatus.CREATED).body(academicPlanningService.saveClassGroup(null, classGroup));
    }

    @PutMapping("/classes/{id}")
    public ClassGroup updateClassGroup(@PathVariable String id, @RequestBody ClassGroup classGroup) {
        return academicPlanningService.saveClassGroup(id, classGroup);
    }

    @DeleteMapping("/classes/{id}")
    public ResponseEntity<Void> deleteClassGroup(@PathVariable String id) {
        academicPlanningService.deleteClassGroup(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/subjects")
    public List<Subject> getSubjects() {
        return academicPlanningService.getSubjects();
    }

    @GetMapping("/subjects/{id}")
    public Subject getSubject(@PathVariable String id) {
        return academicPlanningService.getSubject(id);
    }

    @PostMapping("/subjects")
    public ResponseEntity<Subject> createSubject(@RequestBody Subject subject) {
        return ResponseEntity.status(HttpStatus.CREATED).body(academicPlanningService.saveSubject(null, subject));
    }

    @PutMapping("/subjects/{id}")
    public Subject updateSubject(@PathVariable String id, @RequestBody Subject subject) {
        return academicPlanningService.saveSubject(id, subject);
    }

    @DeleteMapping("/subjects/{id}")
    public ResponseEntity<Void> deleteSubject(@PathVariable String id) {
        academicPlanningService.deleteSubject(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/schedules")
    public List<AcademicSchedule> getAcademicSchedules() {
        return academicPlanningService.getAcademicSchedules();
    }

    @GetMapping("/schedules/{id}")
    public AcademicSchedule getAcademicSchedule(@PathVariable String id) {
        return academicPlanningService.getAcademicSchedule(id);
    }

    @GetMapping("/teachers/{teacherId}/schedules")
    public List<TeacherScheduleView> getTeacherSchedules(@PathVariable String teacherId) {
        return academicPlanningService.getTeacherSchedules(teacherId);
    }

    @PostMapping("/schedules")
    public ResponseEntity<AcademicSchedule> createAcademicSchedule(@RequestBody AcademicSchedule schedule,
                                                                   @RequestParam(defaultValue = "sistema") String actor) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(academicPlanningService.saveAcademicSchedule(null, schedule, actor, null));
    }

    @PutMapping("/schedules/{id}")
    public AcademicSchedule updateAcademicSchedule(@PathVariable String id, @RequestBody AcademicSchedule schedule,
                                                   @RequestParam(defaultValue = "sistema") String actor,
                                                   @RequestParam Integer expectedVersion) {
        return academicPlanningService.saveAcademicSchedule(id, schedule, actor, expectedVersion);
    }

    @GetMapping("/schedules/{id}/versions")
    public List<AcademicSchedule.ScheduleVersion> getScheduleVersions(@PathVariable String id) {
        return academicPlanningService.getScheduleVersions(id);
    }

    @GetMapping("/schedules/{id}/versions/compare")
    public List<AcademicSchedule.ScheduleChange> compareScheduleVersions(
            @PathVariable String id, @RequestParam int fromVersion, @RequestParam int toVersion) {
        return academicPlanningService.compareScheduleVersions(id, fromVersion, toVersion);
    }

    @PostMapping("/schedules/{id}/submit-for-approval")
    public AcademicSchedule submitScheduleForApproval(@PathVariable String id, @RequestParam String actor) {
        return academicPlanningService.submitScheduleForApproval(id, actor);
    }

    @PostMapping("/schedules/{id}/approve")
    public AcademicSchedule approveSchedule(@PathVariable String id, @RequestParam String actor) {
        return academicPlanningService.approveSchedule(id, actor);
    }

    @PostMapping("/schedules/{id}/reject")
    public AcademicSchedule rejectSchedule(@PathVariable String id, @RequestParam String actor,
                                           @RequestParam String justification) {
        return academicPlanningService.rejectSchedule(id, actor, justification);
    }

    @PostMapping("/schedules/{id}/publish")
    public AcademicSchedule publishSchedule(@PathVariable String id, @RequestParam String actor) {
        return academicPlanningService.publishSchedule(id, actor);
    }

    @GetMapping("/schedules/{id}/adjustment-requests")
    public List<AcademicSchedule.AdjustmentRequest> getScheduleAdjustmentRequests(
            @PathVariable String id, HttpServletRequest request) {
        List<AcademicSchedule.AdjustmentRequest> requests =
                academicPlanningService.getScheduleAdjustmentRequests(id);
        PortalUser user = currentUser(request);
        if (user.role() == PortalUser.Role.PROFESSOR) {
            return requests.stream().filter(item -> user.teacherId().equals(item.getRequester())).toList();
        }
        return requests;
    }

    @PostMapping("/schedules/{id}/adjustment-requests")
    public ResponseEntity<AcademicSchedule.AdjustmentRequest> requestScheduleAdjustment(
            @PathVariable String id, @RequestBody AcademicSchedule.AdjustmentRequest request,
            HttpServletRequest servletRequest) {
        PortalUser user = currentUser(servletRequest);
        String requester = user.role() == PortalUser.Role.PROFESSOR
            ? user.teacherId() : request.getRequester();
        if (user.role() == PortalUser.Role.PROFESSOR
            && !teacherOwnsRequestedActivity(id, request.getActivityId(), user.teacherId())) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN,
                "A solicitação deve pertencer a uma atividade do próprio professor");
        }
        AcademicSchedule.AdjustmentRequest saved = academicPlanningService.requestScheduleAdjustment(
            id, requester, request.getActivityId(), request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PatchMapping("/schedules/{id}/adjustment-requests/{requestId}")
    public AcademicSchedule.AdjustmentRequest respondToAdjustment(
            @PathVariable String id, @PathVariable String requestId, @RequestParam String actor,
            @RequestParam String decision, @RequestParam String response) {
        return academicPlanningService.respondToAdjustment(id, requestId, actor, decision, response);
    }

    @GetMapping("/schedules/{id}/notifications")
    public List<AcademicSchedule.ScheduleNotification> getScheduleNotifications(
            @PathVariable String id, @RequestParam String recipient, HttpServletRequest request) {
        PortalUser user = currentUser(request);
        if (user.role() == PortalUser.Role.PROFESSOR && !user.teacherId().equals(recipient)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return academicPlanningService.getScheduleNotifications(id, recipient);
    }

    @PatchMapping("/schedules/{id}/notifications/{notificationId}/read")
        public AcademicSchedule.ScheduleNotification markNotificationRead(
            @PathVariable String id, @PathVariable String notificationId,
            @RequestParam String recipient, HttpServletRequest request) {
        PortalUser user = currentUser(request);
        if (user.role() == PortalUser.Role.PROFESSOR && !user.teacherId().equals(recipient)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return academicPlanningService.markNotificationRead(id, notificationId, recipient);
    }

    @GetMapping("/schedules/{id}/audit")
    public List<AcademicSchedule.ScheduleAuditEvent> getScheduleAudit(@PathVariable String id) {
        return academicPlanningService.getScheduleAudit(id);
    }

    @GetMapping("/schedule-reports")
    public List<AcademicSchedule.ScheduleReport> getScheduleReport(
            @RequestParam(required = false) String academicPeriodId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String teacherId,
            @RequestParam(required = false) String classId,
            @RequestParam(required = false) String roomResourceId) {
        return academicPlanningService.getScheduleReport(
                academicPeriodId, status, teacherId, classId, roomResourceId);
    }

    @DeleteMapping("/schedules/{id}")
    public ResponseEntity<Void> deleteAcademicSchedule(@PathVariable String id) {
        academicPlanningService.deleteAcademicSchedule(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/schedules/{id}/published")
    public AcademicSchedule.ScheduleVersion getPublishedScheduleVersion(@PathVariable String id) {
        return academicPlanningService.getPublishedScheduleVersion(id);
    }

    private PortalUser currentUser(HttpServletRequest request) {
        return (PortalUser) request.getAttribute("portalUser");
    }

    private boolean teacherOwnsRequestedActivity(String scheduleId, String activityId, String teacherId) {
        AcademicSchedule schedule = academicPlanningService.getAcademicSchedule(scheduleId);
        if (schedule.getActivities() == null) {
            return false;
        }
        return schedule.getActivities().stream()
                .filter(activity -> activity.getTeacherIds() != null
                        && activity.getTeacherIds().contains(teacherId))
                .anyMatch(activity -> activityId == null || activityId.equals(activity.getId()));
    }
}