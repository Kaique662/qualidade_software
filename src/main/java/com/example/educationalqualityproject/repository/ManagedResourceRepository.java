package com.example.educationalqualityproject.repository;

import com.example.educationalqualityproject.entity.ManagedResource;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ManagedResourceRepository extends MongoRepository<ManagedResource, String> {
    boolean existsByCode(String code);
    Optional<ManagedResource> findByCode(String code);
}