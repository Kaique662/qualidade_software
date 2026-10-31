package com.example.educationalqualityproject.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

@Document(collection = "teachers")
public class Teacher {

    @Id
    private String id;

    private String name;
    private String email;
    private String department;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private transient Clock clock;

    public Teacher() {
        this(Clock.systemDefaultZone());
    }

    public Teacher(Clock clock) {
        this.clock = Objects.requireNonNull(clock);
        LocalDateTime now = LocalDateTime.now(clock);
        this.createdAt = now;
        this.updatedAt = now;
    }

    public Teacher(String name, String email, String department) {
        this();
        this.name = name;
        this.email = email;
        this.department = department;
    }

    public Teacher(String name, String email, String department, Clock clock) {
        this(clock);
        this.name = name;
        this.email = email;
        this.department = department;
    }

    // Getters and setters
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
        this.updatedAt = LocalDateTime.now(clock);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        this.updatedAt = LocalDateTime.now(clock);
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
        this.updatedAt = LocalDateTime.now(clock);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}