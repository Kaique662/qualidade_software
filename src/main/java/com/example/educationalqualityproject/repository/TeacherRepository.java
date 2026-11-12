package com.example.educationalqualityproject.repository;

import com.example.educationalqualityproject.entity.Teacher;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeacherRepository extends MongoRepository<Teacher, String> {
    boolean existsByEmail(String email);
    boolean existsByEmailIgnoreCase(String email);
    Optional<Teacher> findByEmailIgnoreCase(String email);
}