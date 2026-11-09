package com.example.educationalqualityproject.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Document(collection = "academic_schedules")
public class AcademicSchedule {

    @Id
    private String id;
    private String name;
    private String academicPeriodId;
    private String status = "RASCUNHO";
    private int versionNumber;
    private Integer publishedVersionNumber;
    private LocalDateTime publishedAt;
    private List<ScheduledActivity> activities = new ArrayList<>();
    private List<ScheduleVersion> versions = new ArrayList<>();
    private List<ScheduleAuditEvent> auditEvents = new ArrayList<>();
    private List<AdjustmentRequest> adjustmentRequests = new ArrayList<>();
    private List<ScheduleNotification> notifications = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAcademicPeriodId() {
        return academicPeriodId;
    }

    public void setAcademicPeriodId(String academicPeriodId) {
        this.academicPeriodId = academicPeriodId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    public Integer getPublishedVersionNumber() {
        return publishedVersionNumber;
    }

    public void setPublishedVersionNumber(Integer publishedVersionNumber) {
        this.publishedVersionNumber = publishedVersionNumber;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public List<ScheduledActivity> getActivities() {
        return activities;
    }

    public void setActivities(List<ScheduledActivity> activities) {
        this.activities = activities;
    }

    public List<ScheduleVersion> getVersions() {
        return versions;
    }

    public void setVersions(List<ScheduleVersion> versions) {
        this.versions = versions;
    }

    public List<ScheduleAuditEvent> getAuditEvents() {
        return auditEvents;
    }

    public void setAuditEvents(List<ScheduleAuditEvent> auditEvents) {
        this.auditEvents = auditEvents;
    }

    public List<AdjustmentRequest> getAdjustmentRequests() {
        return adjustmentRequests;
    }

    public void setAdjustmentRequests(List<AdjustmentRequest> adjustmentRequests) {
        this.adjustmentRequests = adjustmentRequests;
    }

    public List<ScheduleNotification> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<ScheduleNotification> notifications) {
        this.notifications = notifications;
    }

    public static class ScheduleVersion {

        private int number;
        private String name;
        private String academicPeriodId;
        private String createdBy;
        private LocalDateTime createdAt;
        private List<ScheduledActivity> activities = new ArrayList<>();

        public int getNumber() { return number; }
        public void setNumber(int number) { this.number = number; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getAcademicPeriodId() { return academicPeriodId; }
        public void setAcademicPeriodId(String academicPeriodId) { this.academicPeriodId = academicPeriodId; }
        public String getCreatedBy() { return createdBy; }
        public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public List<ScheduledActivity> getActivities() { return activities; }
        public void setActivities(List<ScheduledActivity> activities) { this.activities = activities; }
    }

    public static class ScheduleAuditEvent {

        private String action;
        private String actor;
        private String previousStatus;
        private String newStatus;
        private String details;
        private LocalDateTime occurredAt;

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getActor() { return actor; }
        public void setActor(String actor) { this.actor = actor; }
        public String getPreviousStatus() { return previousStatus; }
        public void setPreviousStatus(String previousStatus) { this.previousStatus = previousStatus; }
        public String getNewStatus() { return newStatus; }
        public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
        public String getDetails() { return details; }
        public void setDetails(String details) { this.details = details; }
        public LocalDateTime getOccurredAt() { return occurredAt; }
        public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }
    }

    public static class AdjustmentRequest {

        private String id = UUID.randomUUID().toString();
        private String requester;
        private String activityId;
        private String description;
        private String status = "ABERTA";
        private String response;
        private String respondedBy;
        private LocalDateTime createdAt;
        private LocalDateTime respondedAt;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getRequester() { return requester; }
        public void setRequester(String requester) { this.requester = requester; }
        public String getActivityId() { return activityId; }
        public void setActivityId(String activityId) { this.activityId = activityId; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getResponse() { return response; }
        public void setResponse(String response) { this.response = response; }
        public String getRespondedBy() { return respondedBy; }
        public void setRespondedBy(String respondedBy) { this.respondedBy = respondedBy; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public LocalDateTime getRespondedAt() { return respondedAt; }
        public void setRespondedAt(LocalDateTime respondedAt) { this.respondedAt = respondedAt; }
    }

    public static class ScheduleNotification {

        private String id = UUID.randomUUID().toString();
        private String recipient;
        private String message;
        private LocalDateTime createdAt;
        private boolean read;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getRecipient() { return recipient; }
        public void setRecipient(String recipient) { this.recipient = recipient; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public boolean isRead() { return read; }
        public void setRead(boolean read) { this.read = read; }
    }

    public static class ScheduleChange {

        private String type;
        private String activityId;
        private ScheduledActivity previousActivity;
        private ScheduledActivity newActivity;

        public ScheduleChange() { }

        public ScheduleChange(String type, String activityId, ScheduledActivity previousActivity,
                              ScheduledActivity newActivity) {
            this.type = type;
            this.activityId = activityId;
            this.previousActivity = previousActivity;
            this.newActivity = newActivity;
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getActivityId() { return activityId; }
        public void setActivityId(String activityId) { this.activityId = activityId; }
        public ScheduledActivity getPreviousActivity() { return previousActivity; }
        public void setPreviousActivity(ScheduledActivity previousActivity) { this.previousActivity = previousActivity; }
        public ScheduledActivity getNewActivity() { return newActivity; }
        public void setNewActivity(ScheduledActivity newActivity) { this.newActivity = newActivity; }
    }

    public static class ScheduleReport {

        private String scheduleId;
        private String name;
        private String academicPeriodId;
        private String status;
        private int versionNumber;
        private Integer publishedVersionNumber;
        private LocalDateTime publishedAt;
        private List<ScheduledActivity> activities = new ArrayList<>();

        public String getScheduleId() { return scheduleId; }
        public void setScheduleId(String scheduleId) { this.scheduleId = scheduleId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getAcademicPeriodId() { return academicPeriodId; }
        public void setAcademicPeriodId(String academicPeriodId) { this.academicPeriodId = academicPeriodId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public int getVersionNumber() { return versionNumber; }
        public void setVersionNumber(int versionNumber) { this.versionNumber = versionNumber; }
        public Integer getPublishedVersionNumber() { return publishedVersionNumber; }
        public void setPublishedVersionNumber(Integer publishedVersionNumber) { this.publishedVersionNumber = publishedVersionNumber; }
        public LocalDateTime getPublishedAt() { return publishedAt; }
        public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
        public List<ScheduledActivity> getActivities() { return activities; }
        public void setActivities(List<ScheduledActivity> activities) { this.activities = activities; }
    }

    public static class ScheduledActivity {

        private String id = UUID.randomUUID().toString();
        private String courseId;
        private String classId;
        private String subjectId;
        private List<String> teacherIds = new ArrayList<>();
        private LocalDateTime startsAt;
        private LocalDateTime endsAt;
        private Integer requiredRoomCapacity;
        private List<String> requiredRoomFeatures = new ArrayList<>();
        private String roomResourceId;
        private List<MaterialAssignment> materials = new ArrayList<>();

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getCourseId() {
            return courseId;
        }

        public void setCourseId(String courseId) {
            this.courseId = courseId;
        }

        public String getClassId() {
            return classId;
        }

        public void setClassId(String classId) {
            this.classId = classId;
        }

        public String getSubjectId() {
            return subjectId;
        }

        public void setSubjectId(String subjectId) {
            this.subjectId = subjectId;
        }

        public List<String> getTeacherIds() {
            return teacherIds;
        }

        public void setTeacherIds(List<String> teacherIds) {
            this.teacherIds = teacherIds;
        }

        public LocalDateTime getStartsAt() {
            return startsAt;
        }

        public void setStartsAt(LocalDateTime startsAt) {
            this.startsAt = startsAt;
        }

        public LocalDateTime getEndsAt() {
            return endsAt;
        }

        public void setEndsAt(LocalDateTime endsAt) {
            this.endsAt = endsAt;
        }

        public Integer getRequiredRoomCapacity() {
            return requiredRoomCapacity;
        }

        public void setRequiredRoomCapacity(Integer requiredRoomCapacity) {
            this.requiredRoomCapacity = requiredRoomCapacity;
        }

        public List<String> getRequiredRoomFeatures() {
            return requiredRoomFeatures;
        }

        public void setRequiredRoomFeatures(List<String> requiredRoomFeatures) {
            this.requiredRoomFeatures = requiredRoomFeatures;
        }

        public String getRoomResourceId() {
            return roomResourceId;
        }

        public void setRoomResourceId(String roomResourceId) {
            this.roomResourceId = roomResourceId;
        }

        public List<MaterialAssignment> getMaterials() {
            return materials;
        }

        public void setMaterials(List<MaterialAssignment> materials) {
            this.materials = materials;
        }
    }

    public static class MaterialAssignment {

        private String resourceId;
        private int quantity;

        public String getResourceId() {
            return resourceId;
        }

        public void setResourceId(String resourceId) {
            this.resourceId = resourceId;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }
}