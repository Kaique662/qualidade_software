package com.example.educationalqualityproject.repository;

import com.example.educationalqualityproject.entity.Subject;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SubjectRepository extends MongoRepository<Subject, String> {
}