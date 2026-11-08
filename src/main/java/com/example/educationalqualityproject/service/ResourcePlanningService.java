package com.example.educationalqualityproject.service;

import com.example.educationalqualityproject.entity.AcademicPeriod;
import com.example.educationalqualityproject.entity.AcademicSchedule;
import com.example.educationalqualityproject.entity.ManagedResource;
import com.example.educationalqualityproject.entity.ResourceAgendaEntry;
import com.example.educationalqualityproject.entity.ResourceAllocationRequest;
import com.example.educationalqualityproject.entity.ResourceBlock;
import com.example.educationalqualityproject.entity.TeacherAvailability;
import com.example.educationalqualityproject.repository.AcademicPeriodRepository;
import com.example.educationalqualityproject.repository.AcademicScheduleRepository;
import com.example.educationalqualityproject.repository.ManagedResourceRepository;
import com.example.educationalqualityproject.repository.ResourceBlockRepository;
import com.example.educationalqualityproject.repository.TeacherAvailabilityRepository;
import com.example.educationalqualityproject.repository.TeacherRepository;
import com.example.educationalqualityproject.reservation.ReservationResourceValidator;
import com.example.educationalqualityproject.reservation.ReservationStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

@Service
public class ResourcePlanningService implements ReservationResourceValidator {

    private final ManagedResourceRepository resourceRepository;
    private final TeacherAvailabilityRepository availabilityRepository;
    private final ResourceBlockRepository blockRepository;
    private final AcademicScheduleRepository scheduleRepository;
    private final AcademicPeriodRepository periodRepository;
    private final TeacherRepository teacherRepository;
    private ReservationStore reservationStore;

    public ResourcePlanningService(ManagedResourceRepository resourceRepository,
                                   TeacherAvailabilityRepository availabilityRepository,
                                   ResourceBlockRepository blockRepository,
                                   AcademicScheduleRepository scheduleRepository,
                                   AcademicPeriodRepository periodRepository,
                                   TeacherRepository teacherRepository) {
        this.resourceRepository = resourceRepository;
        this.availabilityRepository = availabilityRepository;
        this.blockRepository = blockRepository;
        this.scheduleRepository = scheduleRepository;
        this.periodRepository = periodRepository;
        this.teacherRepository = teacherRepository;
    }

    @Autowired(required = false)
    public void setReservationStore(ReservationStore reservationStore) {
        this.reservationStore = reservationStore;
    }

    public List<ManagedResource> searchResources(String code, String name,
                                                  ManagedResource.ResourceType type,
                                                  String location,
                                                  ManagedResource.ResourceState state,
                                                  LocalDateTime startsAt,
                                                  LocalDateTime endsAt) {
        validateOptionalInterval(startsAt, endsAt);
        return resourceRepository.findAll().stream()
                .filter(resource -> contains(resource.getCode(), code))
                .filter(resource -> contains(resource.getName(), name))
                .filter(resource -> type == null || type == resource.getType())
                .filter(resource -> contains(resource.getLocation(), location))
                .filter(resource -> state == null || state == resource.getState())
                .filter(resource -> startsAt == null || isAvailable(resource, startsAt, endsAt, 1, null, null))
                .peek(resource -> {
                    if (resource.getType() == ManagedResource.ResourceType.MATERIAL && startsAt != null) {
                        resource.setAvailableQuantity(availableQuantity(resource, startsAt, endsAt, true));
                    }
                })
                .toList();
    }

    public ManagedResource getResource(String id) {
        requireText(id, "Identificador do recurso é obrigatório");
        return resourceRepository.findById(id)
                .orElseThrow(() -> notFound("Recurso não encontrado"));
    }

    public ManagedResource validateReservationResource(String id, LocalDateTime startsAt,
                                                        LocalDateTime endsAt, int quantity) {
        validateInterval(startsAt, endsAt);
        ManagedResource resource = getResource(id);
        requireResourceAvailable(resource, startsAt, endsAt, quantity, null, null);
        return resource;
    }

    public int getAvailableMaterialQuantity(String id, LocalDateTime startsAt, LocalDateTime endsAt) {
        validateInterval(startsAt, endsAt);
        ManagedResource resource = getResource(id);
        if (resource.getType() != ManagedResource.ResourceType.MATERIAL) {
            throw badRequest("O recurso informado não é um material");
        }
        return availableQuantity(resource, startsAt, endsAt, true);
    }

    @Override
    public List<ReservationStore.MaterialInterval> getMaterialReservations(
            String id, LocalDateTime startsAt, LocalDateTime endsAt) {
        validateInterval(startsAt, endsAt);
        ManagedResource resource = getResource(id);
        if (resource.getType() != ManagedResource.ResourceType.MATERIAL) {
            throw badRequest("O recurso informado não é um material");
        }
        LocalDateTime candidateStart = bufferedStart(resource, startsAt);
        LocalDateTime candidateEnd = bufferedEnd(resource, endsAt);
        List<ReservationStore.MaterialInterval> reservations = new ArrayList<>();
        for (AcademicSchedule schedule : scheduleRepository.findAll()) {
            if (schedule.getActivities() == null) {
                continue;
            }
            for (AcademicSchedule.ScheduledActivity activity : schedule.getActivities()) {
                if (activity.getStartsAt() == null || activity.getEndsAt() == null || activity.getMaterials() == null) {
                    continue;
                }
                LocalDateTime reservationStart = bufferedStart(resource, activity.getStartsAt());
                LocalDateTime reservationEnd = bufferedEnd(resource, activity.getEndsAt());
                if (!overlaps(candidateStart, candidateEnd, reservationStart, reservationEnd)) {
                    continue;
                }
                activity.getMaterials().stream()
                        .filter(material -> id.equals(material.getResourceId()))
                        .forEach(material -> reservations.add(new ReservationStore.MaterialInterval(
                                reservationStart.toInstant(ZoneOffset.UTC), reservationEnd.toInstant(ZoneOffset.UTC),
                                material.getQuantity())));
            }
        }
        return reservations;
    }

    public void validateTeacherReservation(String teacherId, String academicPeriodId,
                                           LocalDateTime startsAt, LocalDateTime endsAt) {
        requireReference(teacherRepository.existsById(teacherId), "Professor");
        requireText(academicPeriodId, "Período letivo do professor é obrigatório");
        validateInterval(startsAt, endsAt);
        AcademicSchedule.ScheduledActivity activity = new AcademicSchedule.ScheduledActivity();
        activity.setTeacherIds(List.of(teacherId));
        activity.setStartsAt(startsAt);
        activity.setEndsAt(endsAt);
        validateTeacherConflicts(null, activity);
        validateTeacherAvailability(academicPeriodId, activity);
    }

    public ManagedResource saveResource(String id, ManagedResource resource) {
        requireText(resource.getCode(), "Código do recurso é obrigatório");
        requireText(resource.getName(), "Nome do recurso é obrigatório");
        requireText(resource.getLocation(), "Localização do recurso é obrigatória");
        if (resource.getType() == null) {
            throw badRequest("Tipo do recurso é obrigatório");
        }
        if (resource.getPreparationBufferMinutes() < 0 || resource.getCleanupBufferMinutes() < 0) {
            throw badRequest("Buffers não podem ser negativos");
        }
        if (resource.getType() == ManagedResource.ResourceType.ROOM) {
            if (resource.getCapacity() == null || resource.getCapacity() < 1) {
                throw badRequest("Sala deve ter capacidade positiva");
            }
            resource.setTotalQuantity(null);
            resource.setUnit(null);
        } else {
            if (resource.getTotalQuantity() == null || resource.getTotalQuantity() < 1) {
                throw badRequest("Material deve ter quantidade total positiva");
            }
            requireText(resource.getUnit(), "Unidade do material é obrigatória");
            resource.setCapacity(null);
        }
        if (resource.getFeatures() == null) {
            resource.setFeatures(new ArrayList<>());
        }

        Optional<ManagedResource> sameCode = resourceRepository.findByCode(resource.getCode());
        if (sameCode.isPresent() && !sameCode.get().getId().equals(id)) {
            throw conflict("Já existe recurso com esse código");
        }
        if (id != null) {
            ManagedResource existing = getResource(id);
            resource.setId(existing.getId());
            if (resource.getState() == null) {
                resource.setState(existing.getState());
            }
        } else if (resource.getState() == null) {
            resource.setState(ManagedResource.ResourceState.AVAILABLE);
        }
        return resourceRepository.save(resource);
    }

    public ManagedResource updateResourceState(String id, ManagedResource.ResourceState state) {
        if (state == null) {
            throw badRequest("Estado operacional é obrigatório");
        }
        ManagedResource resource = getResource(id);
        resource.setState(state);
        return resourceRepository.save(resource);
    }

    public void inactivateResource(String id) {
        updateResourceState(id, ManagedResource.ResourceState.INACTIVE);
    }

    public List<TeacherAvailability> getTeacherAvailability(String teacherId, String academicPeriodId) {
        requireReference(teacherRepository.existsById(teacherId), "Professor");
        return availabilityRepository.findAll().stream()
                .filter(window -> teacherId.equals(window.getTeacherId()))
                .filter(window -> academicPeriodId == null || academicPeriodId.equals(window.getAcademicPeriodId()))
                .toList();
    }

    public TeacherAvailability saveTeacherAvailability(String teacherId, String id,
                                                        TeacherAvailability window) {
        requireReference(teacherRepository.existsById(teacherId), "Professor");
        if (!teacherId.equals(window.getTeacherId())) {
            throw badRequest("Professor da janela deve corresponder ao professor da rota");
        }
        requireText(window.getAcademicPeriodId(), "Período letivo é obrigatório");
        AcademicPeriod period = periodRepository.findById(window.getAcademicPeriodId())
                .orElseThrow(() -> badRequest("Período letivo informado não existe"));
        if (!period.isAvailabilityCollectionOpen()) {
            throw conflict("O período de coleta de disponibilidade está encerrado");
        }
        if (window.getType() == null || window.getConstraintKind() == null) {
            throw badRequest("Tipo e classificação da disponibilidade são obrigatórios");
        }
        validateAvailabilityWindow(period, window);
        if (id != null) {
            TeacherAvailability existing = availabilityRepository.findById(id)
                    .orElseThrow(() -> notFound("Disponibilidade não encontrada"));
            if (!teacherId.equals(existing.getTeacherId())) {
                throw notFound("Disponibilidade não encontrada para este professor");
            }
            window.setId(id);
        }
        return availabilityRepository.save(window);
    }

    public void deleteTeacherAvailability(String teacherId, String id) {
        requireReference(teacherRepository.existsById(teacherId), "Professor");
        TeacherAvailability window = availabilityRepository.findById(id)
                .orElseThrow(() -> notFound("Disponibilidade não encontrada"));
        if (!teacherId.equals(window.getTeacherId())) {
            throw notFound("Disponibilidade não encontrada para este professor");
        }
        AcademicPeriod period = periodRepository.findById(window.getAcademicPeriodId())
                .orElseThrow(() -> notFound("Período letivo não encontrado"));
        if (!period.isAvailabilityCollectionOpen()) {
            throw conflict("O período de coleta de disponibilidade está encerrado");
        }
        availabilityRepository.deleteById(id);
    }

    public List<ResourceAgendaEntry> getResourceAgenda(String resourceId,
                                                       LocalDateTime startsAt,
                                                       LocalDateTime endsAt) {
        ManagedResource resource = getResource(resourceId);
        validateInterval(startsAt, endsAt);
        return resourceAgenda(resource, startsAt, endsAt);
    }

    public List<ResourceAgendaEntry> previewBlockImpact(String resourceId,
                                                        LocalDateTime startsAt,
                                                        LocalDateTime endsAt) {
        return getResourceAgenda(resourceId, startsAt, endsAt);
    }

    public ResourceBlock createBlock(ResourceBlock block) {
        requireText(block.getResourceId(), "Recurso do bloqueio é obrigatório");
        ManagedResource resource = getResource(block.getResourceId());
        validateBlock(block);
        // O bloqueio fica pendente para permitir revisar as atividades afetadas antes de ativa-lo.
        block.setStatus(ResourceBlock.BlockStatus.PENDING);
        block.setAffectedAllocationIds(resourceAgenda(resource, block.getStartsAt(), block.getEndsAt()).stream()
                .map(ResourceAgendaEntry::allocationId).toList());
        block.setHistory(new ArrayList<>(List.of(history("CREATED", block.getResponsible()))));
        return blockRepository.save(block);
    }

    public ResourceBlock getBlock(String id) {
        return blockRepository.findById(id).orElseThrow(() -> notFound("Bloqueio não encontrado"));
    }

    public List<ResourceBlock> getResourceBlocks(String resourceId) {
        return blockRepository.findAll().stream()
                .filter(block -> resourceId == null || resourceId.equals(block.getResourceId()))
                .toList();
    }

    public ResourceBlock updateBlock(String id, ResourceBlock update) {
        ResourceBlock block = getBlock(id);
        if (block.getStatus() == ResourceBlock.BlockStatus.CANCELLED) {
            throw conflict("Bloqueio cancelado não pode ser alterado");
        }
        requireText(update.getResourceId(), "Recurso do bloqueio é obrigatório");
        getResource(update.getResourceId());
        validateBlock(update);
        block.setResourceId(update.getResourceId());
        block.setReason(update.getReason());
        block.setResponsible(update.getResponsible());
        block.setStartsAt(update.getStartsAt());
        block.setEndsAt(update.getEndsAt());
        // Alterar recurso ou intervalo pode mudar quais alocacoes serao impactadas.
        block.setAffectedAllocationIds(resourceAgenda(getResource(block.getResourceId()), block.getStartsAt(), block.getEndsAt())
                .stream().map(ResourceAgendaEntry::allocationId).toList());
        ensureBlockHistory(block).add(history("UPDATED", update.getResponsible()));
        return blockRepository.save(block);
    }

    public ResourceBlock confirmBlock(String id, String actor) {
        ResourceBlock block = getBlock(id);
        if (block.getStatus() != ResourceBlock.BlockStatus.PENDING) {
            throw conflict("Somente bloqueios pendentes podem ser confirmados");
        }
        // Recalcula o impacto para incluir alocacoes criadas desde a previa inicial.
        block.setAffectedAllocationIds(resourceAgenda(getResource(block.getResourceId()), block.getStartsAt(), block.getEndsAt())
                .stream().map(ResourceAgendaEntry::allocationId).toList());
        block.setStatus(ResourceBlock.BlockStatus.CONFIRMED);
        ensureBlockHistory(block).add(history("CONFIRMED", actor));
        return blockRepository.save(block);
    }

    public ResourceBlock cancelBlock(String id, String actor) {
        ResourceBlock block = getBlock(id);
        if (block.getStatus() == ResourceBlock.BlockStatus.CANCELLED) {
            throw conflict("Bloqueio já está cancelado");
        }
        // O cancelamento muda o estado, mas preserva o registro e seu historico.
        block.setStatus(ResourceBlock.BlockStatus.CANCELLED);
        ensureBlockHistory(block).add(history("CANCELLED", actor));
        return blockRepository.save(block);
    }

    public AcademicSchedule allocateActivity(String scheduleId, String activityId,
                                             ResourceAllocationRequest request) {
        return allocateActivity(scheduleId, activityId, request, "sistema", null);
    }

    public AcademicSchedule allocateActivity(String scheduleId, String activityId,
                                             ResourceAllocationRequest request, String actor,
                                             Integer expectedVersion) {
        if (request == null) {
            throw badRequest("Dados de alocação são obrigatórios");
        }
        requireText(request.getRoomResourceId(), "Sala da atividade é obrigatória");
        requireText(actor, "Identificador do responsável é obrigatório");
        AcademicSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> notFound("Grade não encontrada"));
        requireEditableSchedule(schedule, expectedVersion);
        AcademicSchedule.ScheduledActivity activity = findActivity(schedule, activityId);
        if (activity.getStartsAt() == null || activity.getEndsAt() == null) {
            throw badRequest("Atividade precisa ter horário antes da alocação");
        }
        validateTeacherConflicts(scheduleId, activity);
        validateTeacherAvailability(schedule.getAcademicPeriodId(), activity);

        ManagedResource room = getResource(request.getRoomResourceId());
        if (room.getType() != ManagedResource.ResourceType.ROOM) {
            throw badRequest("O recurso indicado para sala não é uma sala");
        }
        if (activity.getRequiredRoomCapacity() != null
                && (room.getCapacity() == null || room.getCapacity() < activity.getRequiredRoomCapacity())) {
            throw conflict("A sala não atende à capacidade mínima da atividade");
        }
        List<String> requiredFeatures = activity.getRequiredRoomFeatures() == null
                ? List.of() : activity.getRequiredRoomFeatures();
        List<String> roomFeatures = room.getFeatures() == null ? List.of() : room.getFeatures();
        if (!roomFeatures.containsAll(requiredFeatures)) {
            throw conflict("A sala não atende às características exigidas pela atividade");
        }
        requireResourceAvailable(room, activity.getStartsAt(), activity.getEndsAt(), 1, scheduleId, activityId);

        Map<String, Integer> requestedMaterials = new HashMap<>();
        if (request.getMaterials() == null) {
            throw badRequest("A lista de materiais não pode ser nula");
        }
        for (AcademicSchedule.MaterialAssignment assignment : request.getMaterials()) {
            requireText(assignment.getResourceId(), "Identificador do material é obrigatório");
            if (assignment.getQuantity() < 1) {
                throw badRequest("Quantidade de material deve ser positiva");
            }
            requestedMaterials.merge(assignment.getResourceId(), assignment.getQuantity(), Integer::sum);
        }
        List<AcademicSchedule.MaterialAssignment> validatedMaterials = new ArrayList<>();
        for (Map.Entry<String, Integer> requested : requestedMaterials.entrySet()) {
            ManagedResource material = getResource(requested.getKey());
            if (material.getType() != ManagedResource.ResourceType.MATERIAL) {
                throw badRequest("Recurso indicado como material tem outro tipo");
            }
            requireResourceAvailable(material, activity.getStartsAt(), activity.getEndsAt(),
                    requested.getValue(), scheduleId, activityId);
            AcademicSchedule.MaterialAssignment assignment = new AcademicSchedule.MaterialAssignment();
            assignment.setResourceId(material.getId());
            assignment.setQuantity(requested.getValue());
            validatedMaterials.add(assignment);
        }

        ensureScheduleVersion(schedule, actor);
        activity.setRoomResourceId(room.getId());
        activity.setMaterials(validatedMaterials);
        recordScheduleRevision(schedule, actor, "ALOCACAO_ATUALIZADA");
        scheduleRepository.save(schedule);
        return schedule;
    }

    public void validateScheduleAllocations(AcademicSchedule schedule) {
        if (schedule.getActivities() == null) {
            return;
        }
        for (AcademicSchedule.ScheduledActivity activity : schedule.getActivities()) {
            if (activity.getRoomResourceId() == null
                    && (activity.getMaterials() == null || activity.getMaterials().isEmpty())) {
                continue;
            }
            if (activity.getStartsAt() == null || activity.getEndsAt() == null) {
                throw badRequest("Atividade alocada precisa ter horário válido");
            }
            if (activity.getTeacherIds() == null || activity.getTeacherIds().isEmpty()) {
                throw badRequest("Atividade alocada precisa ter ao menos um professor");
            }
            validateTeacherConflicts(schedule.getId(), activity);
            validateTeacherAvailability(schedule.getAcademicPeriodId(), activity);
            if (activity.getRoomResourceId() != null) {
                ManagedResource room = getResource(activity.getRoomResourceId());
                if (room.getType() != ManagedResource.ResourceType.ROOM) {
                    throw badRequest("Recurso alocado como sala tem outro tipo");
                }
                if (activity.getRequiredRoomCapacity() != null
                        && (room.getCapacity() == null || room.getCapacity() < activity.getRequiredRoomCapacity())) {
                    throw conflict("A sala não atende à capacidade mínima da atividade");
                }
                List<String> features = activity.getRequiredRoomFeatures() == null
                        ? List.of() : activity.getRequiredRoomFeatures();
                List<String> roomFeatures = room.getFeatures() == null ? List.of() : room.getFeatures();
                if (!roomFeatures.containsAll(features)) {
                    throw conflict("A sala não atende às características exigidas pela atividade");
                }
                requireResourceAvailable(room, activity.getStartsAt(), activity.getEndsAt(), 1,
                        schedule.getId(), activity.getId());
            }
            if (activity.getMaterials() != null) {
                for (AcademicSchedule.MaterialAssignment assignment : activity.getMaterials()) {
                if (assignment == null) {
                    throw badRequest("Material da atividade é inválido");
                }
                    requireText(assignment.getResourceId(), "Identificador do material é obrigatório");
                    ManagedResource material = getResource(assignment.getResourceId());
                    if (material.getType() != ManagedResource.ResourceType.MATERIAL
                            || assignment.getQuantity() < 1) {
                        throw badRequest("Material alocado ou quantidade inválida");
                    }
                    requireResourceAvailable(material, activity.getStartsAt(), activity.getEndsAt(),
                            assignment.getQuantity(), schedule.getId(), activity.getId());
                }
            }
        }
    }

    public AcademicSchedule clearActivityAllocation(String scheduleId, String activityId) {
        return clearActivityAllocation(scheduleId, activityId, "sistema", null);
    }

    public AcademicSchedule clearActivityAllocation(String scheduleId, String activityId,
                                                    String actor, Integer expectedVersion) {
        AcademicSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> notFound("Grade não encontrada"));
        requireText(actor, "Identificador do responsável é obrigatório");
        requireEditableSchedule(schedule, expectedVersion);
        AcademicSchedule.ScheduledActivity activity = findActivity(schedule, activityId);
        ensureScheduleVersion(schedule, actor);
        activity.setRoomResourceId(null);
        activity.setMaterials(new ArrayList<>());
        recordScheduleRevision(schedule, actor, "ALOCACAO_REMOVIDA");
        return scheduleRepository.save(schedule);
    }

    private void requireEditableSchedule(AcademicSchedule schedule, Integer expectedVersion) {
        if (!"RASCUNHO".equals(schedule.getStatus())) {
            throw conflict("Alocações só podem ser alteradas enquanto a grade está em rascunho");
        }
        if (expectedVersion != null && expectedVersion != schedule.getVersionNumber()) {
            throw conflict("A grade foi alterada desde a versão consultada; atualize antes de salvar");
        }
    }

    private void ensureScheduleVersion(AcademicSchedule schedule, String actor) {
        if (schedule.getVersions() == null) {
            schedule.setVersions(new ArrayList<>());
        }
        if (schedule.getVersions().isEmpty()) {
            schedule.setVersionNumber(Math.max(1, schedule.getVersionNumber()));
            schedule.getVersions().add(createScheduleVersion(schedule, actor));
        } else if (schedule.getVersionNumber() == 0) {
            schedule.setVersionNumber(schedule.getVersions().stream()
                    .mapToInt(AcademicSchedule.ScheduleVersion::getNumber).max().orElse(1));
        }
        if (schedule.getAuditEvents() == null) {
            schedule.setAuditEvents(new ArrayList<>());
        }
    }

    private void recordScheduleRevision(AcademicSchedule schedule, String actor, String action) {
        schedule.setVersionNumber(schedule.getVersionNumber() + 1);
        schedule.getVersions().add(createScheduleVersion(schedule, actor));
        AcademicSchedule.ScheduleAuditEvent event = new AcademicSchedule.ScheduleAuditEvent();
        event.setAction(action);
        event.setActor(actor);
        event.setPreviousStatus(schedule.getStatus());
        event.setNewStatus(schedule.getStatus());
        event.setDetails("Versão " + schedule.getVersionNumber());
        event.setOccurredAt(LocalDateTime.now());
        schedule.getAuditEvents().add(event);
    }

    private AcademicSchedule.ScheduleVersion createScheduleVersion(AcademicSchedule schedule, String actor) {
        AcademicSchedule.ScheduleVersion version = new AcademicSchedule.ScheduleVersion();
        version.setNumber(schedule.getVersionNumber());
        version.setName(schedule.getName());
        version.setAcademicPeriodId(schedule.getAcademicPeriodId());
        version.setCreatedBy(actor);
        version.setCreatedAt(LocalDateTime.now());
        version.setActivities(schedule.getActivities().stream().map(this::copyActivity).toList());
        return version;
    }

    private AcademicSchedule.ScheduledActivity copyActivity(AcademicSchedule.ScheduledActivity source) {
        AcademicSchedule.ScheduledActivity copy = new AcademicSchedule.ScheduledActivity();
        copy.setId(source.getId());
        copy.setCourseId(source.getCourseId());
        copy.setClassId(source.getClassId());
        copy.setSubjectId(source.getSubjectId());
        copy.setTeacherIds(source.getTeacherIds() == null ? new ArrayList<>() : new ArrayList<>(source.getTeacherIds()));
        copy.setStartsAt(source.getStartsAt());
        copy.setEndsAt(source.getEndsAt());
        copy.setRequiredRoomCapacity(source.getRequiredRoomCapacity());
        copy.setRequiredRoomFeatures(source.getRequiredRoomFeatures() == null
                ? new ArrayList<>() : new ArrayList<>(source.getRequiredRoomFeatures()));
        copy.setRoomResourceId(source.getRoomResourceId());
        List<AcademicSchedule.MaterialAssignment> materials = new ArrayList<>();
        if (source.getMaterials() != null) {
            for (AcademicSchedule.MaterialAssignment material : source.getMaterials()) {
                AcademicSchedule.MaterialAssignment materialCopy = new AcademicSchedule.MaterialAssignment();
                materialCopy.setResourceId(material.getResourceId());
                materialCopy.setQuantity(material.getQuantity());
                materials.add(materialCopy);
            }
        }
        copy.setMaterials(materials);
        return copy;
    }

    private void validateAvailabilityWindow(AcademicPeriod period, TeacherAvailability window) {
        if (window.isRecurringWeekly()) {
            if (window.getValidFrom() == null || window.getValidUntil() == null
                    || window.getValidUntil().isBefore(window.getValidFrom())
                    || window.getValidFrom().isBefore(period.getStartsOn())
                    || window.getValidUntil().isAfter(period.getEndsOn())
                    || window.getDailyStartsAt() == null || window.getDailyEndsAt() == null
                    || !window.getDailyEndsAt().isAfter(window.getDailyStartsAt())
                    || window.getWeekdays() == null || window.getWeekdays().isEmpty()) {
                throw badRequest("Disponibilidade recorrente precisa de período, dias e horários válidos");
            }
        } else if (window.getStartsAt() == null || window.getEndsAt() == null
                || !window.getEndsAt().isAfter(window.getStartsAt())
                || window.getStartsAt().toLocalDate().isBefore(period.getStartsOn())
                || window.getEndsAt().toLocalDate().isAfter(period.getEndsOn())) {
            throw badRequest("Disponibilidade deve ter intervalo válido dentro do período letivo");
        }
    }

    private void validateTeacherAvailability(String periodId, AcademicSchedule.ScheduledActivity activity) {
        if (activity.getTeacherIds() == null || activity.getTeacherIds().isEmpty()) {
            throw badRequest("Atividade precisa ter ao menos um professor associado");
        }
        List<TeacherAvailability> windows = availabilityRepository.findAll().stream()
                .filter(window -> periodId.equals(window.getAcademicPeriodId()))
                .filter(window -> activity.getTeacherIds().contains(window.getTeacherId()))
                .toList();
        for (String teacherId : activity.getTeacherIds()) {
            List<TeacherAvailability> mandatoryAvailable = windows.stream()
                    .filter(window -> teacherId.equals(window.getTeacherId()))
                    .filter(window -> window.getConstraintKind() == TeacherAvailability.ConstraintKind.MANDATORY)
                    .filter(window -> window.getType() == TeacherAvailability.AvailabilityType.AVAILABLE)
                    .toList();
            if (!mandatoryAvailable.isEmpty() && mandatoryAvailable.stream()
                    .noneMatch(window -> contains(window, activity.getStartsAt(), activity.getEndsAt()))) {
                throw conflict("Atividade está fora da disponibilidade obrigatória do professor " + teacherId);
            }
            boolean unavailable = windows.stream()
                    .filter(window -> teacherId.equals(window.getTeacherId()))
                    .filter(window -> window.getConstraintKind() == TeacherAvailability.ConstraintKind.MANDATORY)
                    .filter(window -> window.getType() == TeacherAvailability.AvailabilityType.UNAVAILABLE)
                    .anyMatch(window -> overlaps(window, activity.getStartsAt(), activity.getEndsAt()));
            if (unavailable) {
                throw conflict("Professor possui indisponibilidade obrigatória no horário da atividade");
            }
        }
    }

    private boolean contains(TeacherAvailability window, LocalDateTime startsAt, LocalDateTime endsAt) {
        if (!window.isRecurringWeekly()) {
            return !startsAt.isBefore(window.getStartsAt()) && !endsAt.isAfter(window.getEndsAt());
        }
        if (startsAt.toLocalDate().equals(endsAt.toLocalDate())
                && !startsAt.toLocalDate().isBefore(window.getValidFrom())
                && !startsAt.toLocalDate().isAfter(window.getValidUntil())
                && window.getWeekdays().contains(startsAt.getDayOfWeek())) {
            LocalDate date = startsAt.toLocalDate();
            return !startsAt.isBefore(date.atTime(window.getDailyStartsAt()))
                    && !endsAt.isAfter(date.atTime(window.getDailyEndsAt()));
        }
        return false;
    }

    private boolean overlaps(TeacherAvailability window, LocalDateTime startsAt, LocalDateTime endsAt) {
        if (!window.isRecurringWeekly()) {
            return overlaps(startsAt, endsAt, window.getStartsAt(), window.getEndsAt());
        }
        for (LocalDate date = startsAt.toLocalDate(); !date.isAfter(endsAt.toLocalDate()); date = date.plusDays(1)) {
            if (!date.isBefore(window.getValidFrom()) && !date.isAfter(window.getValidUntil())
                    && window.getWeekdays().contains(date.getDayOfWeek())) {
                LocalDateTime windowStart = date.atTime(window.getDailyStartsAt());
                LocalDateTime windowEnd = date.atTime(window.getDailyEndsAt());
                if (overlaps(startsAt, endsAt, windowStart, windowEnd)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void validateTeacherConflicts(String scheduleId, AcademicSchedule.ScheduledActivity candidate) {
        for (AcademicSchedule existing : scheduleRepository.findAll()) {
            if (existing.getActivities() == null) {
                continue;
            }
            for (AcademicSchedule.ScheduledActivity other : existing.getActivities()) {
                if (scheduleId != null && scheduleId.equals(existing.getId())
                                && candidate.getId().equals(other.getId())) {
                    continue;
                }
                if (other.getStartsAt() != null && other.getEndsAt() != null
                        && overlaps(candidate.getStartsAt(), candidate.getEndsAt(), other.getStartsAt(), other.getEndsAt())
                        && intersects(candidate.getTeacherIds(), other.getTeacherIds())) {
                    throw conflict("Professor já possui atividade sobreposta na grade " + existing.getId());
                }
            }
        }
    }

    private void requireResourceAvailable(ManagedResource resource, LocalDateTime startsAt,
                                         LocalDateTime endsAt, int quantity,
                                         String excludedScheduleId, String excludedActivityId) {
        if (resource.getState() != ManagedResource.ResourceState.AVAILABLE) {
            throw conflict("Recurso não está operacionalmente disponível");
        }
        LocalDateTime bufferedStart = bufferedStart(resource, startsAt);
        LocalDateTime bufferedEnd = bufferedEnd(resource, endsAt);
        // A mesma checagem considera estado operacional, bloqueios confirmados e reservas existentes.
        boolean blocked = blockRepository.findAll().stream()
                .filter(block -> resource.getId().equals(block.getResourceId()))
                .filter(block -> block.getStatus() == ResourceBlock.BlockStatus.CONFIRMED)
                .anyMatch(block -> overlaps(bufferedStart, bufferedEnd, block.getStartsAt(), block.getEndsAt()));
        if (blocked) {
            throw conflict("Recurso possui bloqueio confirmado no intervalo solicitado");
        }

        // A reserva atual e comparada com as demais usando os buffers configurados no recurso.
        List<QuantityInterval> reservations = new ArrayList<>();
        for (AcademicSchedule schedule : scheduleRepository.findAll()) {
            if (schedule.getActivities() == null) {
                continue;
            }
            for (AcademicSchedule.ScheduledActivity activity : schedule.getActivities()) {
                if (schedule.getId().equals(excludedScheduleId) && activity.getId().equals(excludedActivityId)) {
                    continue;
                }
                if (activity.getStartsAt() == null || activity.getEndsAt() == null) {
                    continue;
                }
                if (resource.getType() == ManagedResource.ResourceType.ROOM
                        && resource.getId().equals(activity.getRoomResourceId())) {
                    reservations.add(new QuantityInterval(bufferedStart(resource, activity.getStartsAt()),
                            bufferedEnd(resource, activity.getEndsAt()), 1));
                }
                if (resource.getType() == ManagedResource.ResourceType.MATERIAL && activity.getMaterials() != null) {
                    activity.getMaterials().stream()
                            .filter(material -> resource.getId().equals(material.getResourceId()))
                            .forEach(material -> reservations.add(new QuantityInterval(
                                    bufferedStart(resource, activity.getStartsAt()),
                                    bufferedEnd(resource, activity.getEndsAt()), material.getQuantity())));
                }
            }
        }
        if (resource.getType() == ManagedResource.ResourceType.MATERIAL && reservationStore != null) {
            reservationStore.findActiveMaterialReservations(resource.getId(), bufferedStart.toInstant(ZoneOffset.UTC),
                    bufferedEnd.toInstant(ZoneOffset.UTC))
                .forEach(interval -> reservations.add(new QuantityInterval(
                    LocalDateTime.ofInstant(interval.startsAt(), ZoneOffset.UTC),
                    LocalDateTime.ofInstant(interval.endsAt(), ZoneOffset.UTC), interval.quantity())));
        }
        int capacity = resource.getType() == ManagedResource.ResourceType.ROOM ? 1 : resource.getTotalQuantity();
        if (capacity == 0 || maxConcurrentQuantity(reservations, bufferedStart, bufferedEnd, quantity) > capacity) {
            throw conflict(resource.getType() == ManagedResource.ResourceType.ROOM
                    ? "Sala já está alocada no intervalo solicitado"
                    : "Quantidade de material excede o estoque disponível no intervalo");
        }
    }

    private boolean isAvailable(ManagedResource resource, LocalDateTime startsAt, LocalDateTime endsAt,
                                int quantity, String excludedScheduleId, String excludedActivityId) {
        try {
            requireResourceAvailable(resource, startsAt, endsAt, quantity, excludedScheduleId, excludedActivityId);
            return true;
        } catch (ResponseStatusException exception) {
            if (exception.getStatusCode() == HttpStatus.CONFLICT) {
                return false;
            }
            throw exception;
        }
    }

    private int maxConcurrentQuantity(List<QuantityInterval> reservations,
                                      LocalDateTime candidateStart,
                                      LocalDateTime candidateEnd,
                                      int candidateQuantity) {
        // Soma eventos de inicio/fim para obter o pico simultaneo, nao a soma de reservas sequenciais.
        TreeMap<LocalDateTime, Integer> events = new TreeMap<>();
        addEvents(events, candidateStart, candidateEnd, candidateQuantity);
        for (QuantityInterval reservation : reservations) {
            if (overlaps(candidateStart, candidateEnd, reservation.startsAt(), reservation.endsAt())) {
                addEvents(events, reservation.startsAt(), reservation.endsAt(), reservation.quantity());
            }
        }
        int current = 0;
        int maximum = 0;
        for (int delta : events.values()) {
            current += delta;
            maximum = Math.max(maximum, current);
        }
        return maximum;
    }

    private int availableQuantity(ManagedResource resource, LocalDateTime startsAt, LocalDateTime endsAt,
                                  boolean includeSqlReservations) {
        LocalDateTime candidateStart = bufferedStart(resource, startsAt);
        LocalDateTime candidateEnd = bufferedEnd(resource, endsAt);
        TreeMap<LocalDateTime, Integer> events = new TreeMap<>();
        for (AcademicSchedule schedule : scheduleRepository.findAll()) {
            if (schedule.getActivities() == null) {
                continue;
            }
            for (AcademicSchedule.ScheduledActivity activity : schedule.getActivities()) {
                if (activity.getStartsAt() == null || activity.getEndsAt() == null || activity.getMaterials() == null) {
                    continue;
                }
                LocalDateTime reservedStart = bufferedStart(resource, activity.getStartsAt());
                LocalDateTime reservedEnd = bufferedEnd(resource, activity.getEndsAt());
                if (!overlaps(candidateStart, candidateEnd, reservedStart, reservedEnd)) {
                    continue;
                }
                activity.getMaterials().stream()
                        .filter(material -> resource.getId().equals(material.getResourceId()))
                        .forEach(material -> addEvents(events,
                                reservedStart.isBefore(candidateStart) ? candidateStart : reservedStart,
                                reservedEnd.isAfter(candidateEnd) ? candidateEnd : reservedEnd,
                                material.getQuantity()));
            }
        }
        if (includeSqlReservations && reservationStore != null) {
            reservationStore.findActiveMaterialReservations(resource.getId(), candidateStart.toInstant(ZoneOffset.UTC),
                    candidateEnd.toInstant(ZoneOffset.UTC))
                .forEach(interval -> addEvents(events,
                    LocalDateTime.ofInstant(interval.startsAt(), ZoneOffset.UTC),
                    LocalDateTime.ofInstant(interval.endsAt(), ZoneOffset.UTC), interval.quantity()));
        }
        int reserved = 0;
        int maximum = 0;
        for (int delta : events.values()) {
            reserved += delta;
            maximum = Math.max(maximum, reserved);
        }
        return Math.max(0, resource.getTotalQuantity() - maximum);
    }

    private void addEvents(TreeMap<LocalDateTime, Integer> events, LocalDateTime startsAt,
                           LocalDateTime endsAt, int quantity) {
        events.merge(startsAt, quantity, Integer::sum);
        events.merge(endsAt, -quantity, Integer::sum);
    }

    private List<ResourceAgendaEntry> resourceAgenda(ManagedResource resource,
                                                     LocalDateTime startsAt,
                                                     LocalDateTime endsAt) {
        // Retorna somente atividades do recurso que cruzam o intervalo, incluindo os buffers.
        List<ResourceAgendaEntry> entries = new ArrayList<>();
        for (AcademicSchedule schedule : scheduleRepository.findAll()) {
            if (schedule.getActivities() == null) {
                continue;
            }
            for (AcademicSchedule.ScheduledActivity activity : schedule.getActivities()) {
                if (activity.getStartsAt() == null || activity.getEndsAt() == null
                        || !overlaps(bufferedStart(resource, startsAt), bufferedEnd(resource, endsAt),
                        bufferedStart(resource, activity.getStartsAt()), bufferedEnd(resource, activity.getEndsAt()))) {
                    continue;
                }
                if (resource.getType() == ManagedResource.ResourceType.ROOM
                        && resource.getId().equals(activity.getRoomResourceId())) {
                    entries.add(toAgendaEntry(schedule, activity, resource, 1));
                }
                if (resource.getType() == ManagedResource.ResourceType.MATERIAL && activity.getMaterials() != null) {
                    activity.getMaterials().stream()
                            .filter(material -> resource.getId().equals(material.getResourceId()))
                            .forEach(material -> entries.add(toAgendaEntry(schedule, activity, resource, material.getQuantity())));
                }
            }
        }
        return entries;
    }

    private ResourceAgendaEntry toAgendaEntry(AcademicSchedule schedule,
                                              AcademicSchedule.ScheduledActivity activity,
                                              ManagedResource resource, int quantity) {
        return new ResourceAgendaEntry(schedule.getId() + "/" + activity.getId(), schedule.getId(),
                resource.getId(), resource.getType().name(), activity.getCourseId(), activity.getClassId(),
                activity.getSubjectId(), activity.getStartsAt(), activity.getEndsAt(), quantity);
    }

    private AcademicSchedule.ScheduledActivity findActivity(AcademicSchedule schedule, String activityId) {
        if (schedule.getActivities() == null) {
            throw notFound("Atividade não encontrada na grade");
        }
        return schedule.getActivities().stream()
                .filter(activity -> activityId.equals(activity.getId()))
                .findFirst().orElseThrow(() -> notFound("Atividade não encontrada na grade"));
    }

    private void validateBlock(ResourceBlock block) {
        requireText(block.getReason(), "Motivo do bloqueio é obrigatório");
        requireText(block.getResponsible(), "Responsável pelo bloqueio é obrigatório");
        validateInterval(block.getStartsAt(), block.getEndsAt());
    }

    private void validateOptionalInterval(LocalDateTime startsAt, LocalDateTime endsAt) {
        if ((startsAt == null) != (endsAt == null)) {
            throw badRequest("Informe início e fim para filtrar disponibilidade");
        }
        if (startsAt != null) {
            validateInterval(startsAt, endsAt);
        }
    }

    private void validateInterval(LocalDateTime startsAt, LocalDateTime endsAt) {
        if (startsAt == null || endsAt == null || !endsAt.isAfter(startsAt)) {
            throw badRequest("Intervalo deve ter início e fim válidos");
        }
    }

    private boolean contains(String value, String filter) {
        return filter == null || (value != null && value.toLowerCase().contains(filter.toLowerCase()));
    }

    private boolean overlaps(LocalDateTime startA, LocalDateTime endA,
                             LocalDateTime startB, LocalDateTime endB) {
        return startA.isBefore(endB) && endA.isAfter(startB);
    }

    private boolean intersects(List<String> first, List<String> second) {
        if (first == null || second == null) {
            return false;
        }
        Set<String> secondIds = new HashSet<>(second);
        return first.stream().anyMatch(secondIds::contains);
    }

    private LocalDateTime bufferedStart(ManagedResource resource, LocalDateTime startsAt) {
        return startsAt.minusMinutes(resource.getPreparationBufferMinutes());
    }

    private LocalDateTime bufferedEnd(ManagedResource resource, LocalDateTime endsAt) {
        return endsAt.plusMinutes(resource.getCleanupBufferMinutes());
    }

    private ResourceBlock.BlockHistoryEntry history(String action, String actor) {
        return new ResourceBlock.BlockHistoryEntry(action, actor, LocalDateTime.now(ZoneOffset.UTC));
    }

    private List<ResourceBlock.BlockHistoryEntry> ensureBlockHistory(ResourceBlock block) {
        if (block.getHistory() == null) {
            block.setHistory(new ArrayList<>());
        }
        return block.getHistory();
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw badRequest(message);
        }
    }

    private void requireReference(boolean exists, String name) {
        if (!exists) {
            throw badRequest(name + " informado não existe");
        }
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    private record QuantityInterval(LocalDateTime startsAt, LocalDateTime endsAt, int quantity) { }
}