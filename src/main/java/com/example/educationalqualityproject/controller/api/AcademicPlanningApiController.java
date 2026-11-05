package com.example.educationalqualityproject.controller.api;

import com.example.educationalqualityproject.entity.AcademicPeriod;
import com.example.educationalqualityproject.entity.AcademicSchedule;
import com.example.educationalqualityproject.entity.ClassGroup;
import com.example.educationalqualityproject.entity.Course;
import com.example.educationalqualityproject.entity.Subject;
import com.example.educationalqualityproject.service.AcademicPlanningService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AcademicPlanningApiController {

    private final AcademicPlanningService academicPlanningService;

    public AcademicPlanningApiController(AcademicPlanningService academicPlanningService) {
        this.academicPlanningService = academicPlanningService;
    }

    @GetMapping("/academic-periods")
    public List<AcademicPeriod> getAcademicPeriods() {
        return academicPlanningService.getAcademicPeriods();
    }

    @GetMapping("/academic-periods/{id}")
    public AcademicPeriod getAcademicPeriod(@PathVariable String id) {
        return academicPlanningService.getAcademicPeriod(id);
    }

    @PostMapping("/academic-periods")
    public ResponseEntity<AcademicPeriod> createAcademicPeriod(@RequestBody AcademicPeriod period) {
        return ResponseEntity.status(HttpStatus.CREATED).body(academicPlanningService.saveAcademicPeriod(null, period));
    }

    @PutMapping("/academic-periods/{id}")
    public AcademicPeriod updateAcademicPeriod(@PathVariable String id, @RequestBody AcademicPeriod period) {
        return academicPlanningService.saveAcademicPeriod(id, period);
    }

    @DeleteMapping("/academic-periods/{id}")
    public ResponseEntity<Void> deleteAcademicPeriod(@PathVariable String id) {
        academicPlanningService.deleteAcademicPeriod(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/courses")
    public List<Course> getCourses() {
        return academicPlanningService.getCourses();
    }

    @GetMapping("/courses/{id}")
    public Course getCourse(@PathVariable String id) {
        return academicPlanningService.getCourse(id);
    }

    @PostMapping("/courses")
    public ResponseEntity<Course> createCourse(@RequestBody Course course) {
        return ResponseEntity.status(HttpStatus.CREATED).body(academicPlanningService.saveCourse(null, course));
    }

    @PutMapping("/courses/{id}")
    public Course updateCourse(@PathVariable String id, @RequestBody Course course) {
        return academicPlanningService.saveCourse(id, course);
    }

    @DeleteMapping("/courses/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable String id) {
        academicPlanningService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/classes")
    public List<ClassGroup> getClassGroups() {
        return academicPlanningService.getClassGroups();
    }

    @GetMapping("/classes/{id}")
    public ClassGroup getClassGroup(@PathVariable String id) {
        return academicPlanningService.getClassGroup(id);
    }

    @PostMapping("/classes")
    public ResponseEntity<ClassGroup> createClassGroup(@RequestBody ClassGroup classGroup) {
        return ResponseEntity.status(HttpStatus.CREATED).body(academicPlanningService.saveClassGroup(null, classGroup));
    }

    @PutMapping("/classes/{id}")
    public ClassGroup updateClassGroup(@PathVariable String id, @RequestBody ClassGroup classGroup) {
        return academicPlanningService.saveClassGroup(id, classGroup);
    }

    @DeleteMapping("/classes/{id}")
    public ResponseEntity<Void> deleteClassGroup(@PathVariable String id) {
        academicPlanningService.deleteClassGroup(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/subjects")
    public List<Subject> getSubjects() {
        return academicPlanningService.getSubjects();
    }

    @GetMapping("/subjects/{id}")
    public Subject getSubject(@PathVariable String id) {
        return academicPlanningService.getSubject(id);
    }

    @PostMapping("/subjects")
    public ResponseEntity<Subject> createSubject(@RequestBody Subject subject) {
        return ResponseEntity.status(HttpStatus.CREATED).body(academicPlanningService.saveSubject(null, subject));
    }

    @PutMapping("/subjects/{id}")
    public Subject updateSubject(@PathVariable String id, @RequestBody Subject subject) {
        return academicPlanningService.saveSubject(id, subject);
    }

    @DeleteMapping("/subjects/{id}")
    public ResponseEntity<Void> deleteSubject(@PathVariable String id) {
        academicPlanningService.deleteSubject(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/schedules")
    public List<AcademicSchedule> getAcademicSchedules() {
        return academicPlanningService.getAcademicSchedules();
    }

    @GetMapping("/schedules/{id}")
    public AcademicSchedule getAcademicSchedule(@PathVariable String id) {
        return academicPlanningService.getAcademicSchedule(id);
    }

    @PostMapping("/schedules")
    public ResponseEntity<AcademicSchedule> createAcademicSchedule(@RequestBody AcademicSchedule schedule) {
        return ResponseEntity.status(HttpStatus.CREATED).body(academicPlanningService.saveAcademicSchedule(null, schedule));
    }

    @PutMapping("/schedules/{id}")
    public AcademicSchedule updateAcademicSchedule(@PathVariable String id, @RequestBody AcademicSchedule schedule) {
        return academicPlanningService.saveAcademicSchedule(id, schedule);
    }

    @DeleteMapping("/schedules/{id}")
    public ResponseEntity<Void> deleteAcademicSchedule(@PathVariable String id) {
        academicPlanningService.deleteAcademicSchedule(id);
        return ResponseEntity.noContent().build();
    }
}