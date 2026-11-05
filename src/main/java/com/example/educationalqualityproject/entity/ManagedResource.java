package com.example.educationalqualityproject.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "managed_resources")
public class ManagedResource {

    @Id
    private String id;
    private String code;
    private String name;
    private ResourceType type;
    private String location;
    private Integer capacity;
    private Integer totalQuantity;
    @Transient
    private Integer availableQuantity;
    private String unit;
    private List<String> features = new ArrayList<>();
    private ResourceState state = ResourceState.AVAILABLE;
    private int preparationBufferMinutes;
    private int cleanupBufferMinutes;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public ResourceType getType() { return type; }
    public void setType(ResourceType type) { this.type = type; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public Integer getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(Integer totalQuantity) { this.totalQuantity = totalQuantity; }
    public Integer getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public List<String> getFeatures() { return features; }
    public void setFeatures(List<String> features) { this.features = features; }
    public ResourceState getState() { return state; }
    public void setState(ResourceState state) { this.state = state; }
    public int getPreparationBufferMinutes() { return preparationBufferMinutes; }
    public void setPreparationBufferMinutes(int preparationBufferMinutes) { this.preparationBufferMinutes = preparationBufferMinutes; }
    public int getCleanupBufferMinutes() { return cleanupBufferMinutes; }
    public void setCleanupBufferMinutes(int cleanupBufferMinutes) { this.cleanupBufferMinutes = cleanupBufferMinutes; }

    public enum ResourceType {
        ROOM,
        MATERIAL
    }

    public enum ResourceState {
        AVAILABLE,
        IN_USE,
        MAINTENANCE,
        INACTIVE
    }
}