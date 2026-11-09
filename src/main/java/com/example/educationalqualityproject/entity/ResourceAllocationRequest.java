package com.example.educationalqualityproject.entity;

import java.util.ArrayList;
import java.util.List;

public class ResourceAllocationRequest {

    private String roomResourceId;
    private List<AcademicSchedule.MaterialAssignment> materials = new ArrayList<>();

    public String getRoomResourceId() {
        return roomResourceId;
    }

    public void setRoomResourceId(String roomResourceId) {
        this.roomResourceId = roomResourceId;
    }

    public List<AcademicSchedule.MaterialAssignment> getMaterials() {
        return materials;
    }

    public void setMaterials(List<AcademicSchedule.MaterialAssignment> materials) {
        this.materials = materials;
    }
}