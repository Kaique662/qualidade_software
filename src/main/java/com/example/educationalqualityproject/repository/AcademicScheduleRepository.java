package com.example.educationalqualityproject.repository;

import com.example.educationalqualityproject.entity.AcademicSchedule;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AcademicScheduleRepository extends MongoRepository<AcademicSchedule, String> {
}