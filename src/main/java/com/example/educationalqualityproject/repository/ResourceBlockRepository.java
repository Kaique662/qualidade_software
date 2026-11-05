package com.example.educationalqualityproject.repository;

import com.example.educationalqualityproject.entity.ResourceBlock;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ResourceBlockRepository extends MongoRepository<ResourceBlock, String> {
}