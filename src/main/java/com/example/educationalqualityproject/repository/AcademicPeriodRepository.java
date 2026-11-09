package com.example.educationalqualityproject.repository;

import com.example.educationalqualityproject.entity.AcademicPeriod;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AcademicPeriodRepository extends MongoRepository<AcademicPeriod, String> {
}