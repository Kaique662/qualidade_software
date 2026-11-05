package com.example.educationalqualityproject.controller.api;

import com.example.educationalqualityproject.entity.AcademicSchedule;
import com.example.educationalqualityproject.entity.ManagedResource;
import com.example.educationalqualityproject.entity.ResourceAgendaEntry;
import com.example.educationalqualityproject.entity.ResourceAllocationRequest;
import com.example.educationalqualityproject.entity.ResourceBlock;
import com.example.educationalqualityproject.entity.TeacherAvailability;
import com.example.educationalqualityproject.service.ResourcePlanningService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ResourcePlanningApiController {

    private final ResourcePlanningService resourcePlanningService;

    public ResourcePlanningApiController(ResourcePlanningService resourcePlanningService) {
        this.resourcePlanningService = resourcePlanningService;
    }

    @GetMapping("/resources")
    public List<ManagedResource> searchResources(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) ManagedResource.ResourceType type,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) ManagedResource.ResourceState state,
            @RequestParam(required = false) LocalDateTime startsAt,
            @RequestParam(required = false) LocalDateTime endsAt) {
        return resourcePlanningService.searchResources(code, name, type, location, state, startsAt, endsAt);
    }

    @GetMapping("/resources/{id}")
    public ManagedResource getResource(@PathVariable String id) {
        return resourcePlanningService.getResource(id);
    }

    @PostMapping("/resources")
    public ResponseEntity<ManagedResource> createResource(@RequestBody ManagedResource resource) {
        return ResponseEntity.status(HttpStatus.CREATED).body(resourcePlanningService.saveResource(null, resource));
    }

    @PutMapping("/resources/{id}")
    public ManagedResource updateResource(@PathVariable String id, @RequestBody ManagedResource resource) {
        return resourcePlanningService.saveResource(id, resource);
    }

    @DeleteMapping("/resources/{id}")
    public ResponseEntity<Void> inactivateResource(@PathVariable String id) {
        resourcePlanningService.inactivateResource(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/resources/{id}/state")
    public ManagedResource updateResourceState(@PathVariable String id,
                                               @RequestParam ManagedResource.ResourceState state) {
        return resourcePlanningService.updateResourceState(id, state);
    }

    @GetMapping("/teachers/{teacherId}/availability")
    public List<TeacherAvailability> getTeacherAvailability(
            @PathVariable String teacherId,
            @RequestParam(required = false) String academicPeriodId) {
        return resourcePlanningService.getTeacherAvailability(teacherId, academicPeriodId);
    }

    @PostMapping("/teachers/{teacherId}/availability")
    public ResponseEntity<TeacherAvailability> createTeacherAvailability(
            @PathVariable String teacherId, @RequestBody TeacherAvailability availability) {
        availability.setTeacherId(teacherId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resourcePlanningService.saveTeacherAvailability(teacherId, null, availability));
    }

    @PutMapping("/teachers/{teacherId}/availability/{availabilityId}")
    public TeacherAvailability updateTeacherAvailability(
            @PathVariable String teacherId,
            @PathVariable String availabilityId,
            @RequestBody TeacherAvailability availability) {
        availability.setTeacherId(teacherId);
        return resourcePlanningService.saveTeacherAvailability(teacherId, availabilityId, availability);
    }

    @DeleteMapping("/teachers/{teacherId}/availability/{availabilityId}")
    public ResponseEntity<Void> deleteTeacherAvailability(
            @PathVariable String teacherId, @PathVariable String availabilityId) {
        resourcePlanningService.deleteTeacherAvailability(teacherId, availabilityId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/resources/{resourceId}/agenda")
    public List<ResourceAgendaEntry> getResourceAgenda(
            @PathVariable String resourceId,
            @RequestParam LocalDateTime startsAt,
            @RequestParam LocalDateTime endsAt) {
        return resourcePlanningService.getResourceAgenda(resourceId, startsAt, endsAt);
    }

    @GetMapping("/resources/{resourceId}/block-impact")
    public List<ResourceAgendaEntry> previewBlockImpact(
            @PathVariable String resourceId,
            @RequestParam LocalDateTime startsAt,
            @RequestParam LocalDateTime endsAt) {
        return resourcePlanningService.previewBlockImpact(resourceId, startsAt, endsAt);
    }

    @PostMapping("/resource-blocks")
    public ResponseEntity<ResourceBlock> createBlock(@RequestBody ResourceBlock block) {
        return ResponseEntity.status(HttpStatus.CREATED).body(resourcePlanningService.createBlock(block));
    }

    @GetMapping("/resource-blocks/{id}")
    public ResourceBlock getBlock(@PathVariable String id) {
        return resourcePlanningService.getBlock(id);
    }

    @PutMapping("/resource-blocks/{id}")
    public ResourceBlock updateBlock(@PathVariable String id, @RequestBody ResourceBlock block) {
        return resourcePlanningService.updateBlock(id, block);
    }

    @PostMapping("/resource-blocks/{id}/confirm")
    public ResourceBlock confirmBlock(@PathVariable String id, @RequestParam String actor) {
        return resourcePlanningService.confirmBlock(id, actor);
    }

    @PostMapping("/resource-blocks/{id}/cancel")
    public ResourceBlock cancelBlock(@PathVariable String id, @RequestParam String actor) {
        return resourcePlanningService.cancelBlock(id, actor);
    }

    @PutMapping("/schedules/{scheduleId}/activities/{activityId}/allocation")
    public AcademicSchedule allocateActivity(
            @PathVariable String scheduleId,
            @PathVariable String activityId,
            @RequestBody ResourceAllocationRequest request) {
        return resourcePlanningService.allocateActivity(scheduleId, activityId, request);
    }

    @DeleteMapping("/schedules/{scheduleId}/activities/{activityId}/allocation")
    public AcademicSchedule clearActivityAllocation(
            @PathVariable String scheduleId, @PathVariable String activityId) {
        return resourcePlanningService.clearActivityAllocation(scheduleId, activityId);
    }
}