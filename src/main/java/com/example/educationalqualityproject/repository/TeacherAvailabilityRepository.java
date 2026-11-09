package com.example.educationalqualityproject.repository;

import com.example.educationalqualityproject.entity.TeacherAvailability;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TeacherAvailabilityRepository extends MongoRepository<TeacherAvailability, String> {
}