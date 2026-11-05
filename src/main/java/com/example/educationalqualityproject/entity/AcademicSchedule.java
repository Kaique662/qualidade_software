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
    private List<ScheduledActivity> activities = new ArrayList<>();

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

    public List<ScheduledActivity> getActivities() {
        return activities;
    }

    public void setActivities(List<ScheduledActivity> activities) {
        this.activities = activities;
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