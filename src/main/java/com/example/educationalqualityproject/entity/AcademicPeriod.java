package com.example.educationalqualityproject.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "academic_periods")
public class AcademicPeriod {

    @Id
    private String id;
    private String name;
    private LocalDate startsOn;
    private LocalDate endsOn;
    private boolean availabilityCollectionOpen = true;

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

    public LocalDate getStartsOn() {
        return startsOn;
    }

    public void setStartsOn(LocalDate startsOn) {
        this.startsOn = startsOn;
    }

    public LocalDate getEndsOn() {
        return endsOn;
    }

    public void setEndsOn(LocalDate endsOn) {
        this.endsOn = endsOn;
    }

    public boolean isAvailabilityCollectionOpen() {
        return availabilityCollectionOpen;
    }

    public void setAvailabilityCollectionOpen(boolean availabilityCollectionOpen) {
        this.availabilityCollectionOpen = availabilityCollectionOpen;
    }
}