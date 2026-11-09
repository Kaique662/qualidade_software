package com.example.educationalqualityproject.repository;

import com.example.educationalqualityproject.entity.Course;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CourseRepository extends MongoRepository<Course, String> {
}