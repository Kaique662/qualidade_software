package com.example.educationalqualityproject.repository;

import com.example.educationalqualityproject.entity.ClassGroup;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ClassGroupRepository extends MongoRepository<ClassGroup, String> {
}