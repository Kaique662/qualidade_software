package com.example.educationalqualityproject.service;

import com.example.educationalqualityproject.entity.AcademicPeriod;
import com.example.educationalqualityproject.entity.AcademicSchedule;
import com.example.educationalqualityproject.entity.ClassGroup;
import com.example.educationalqualityproject.entity.Course;
import com.example.educationalqualityproject.entity.Subject;
import com.example.educationalqualityproject.repository.AcademicPeriodRepository;
import com.example.educationalqualityproject.repository.AcademicScheduleRepository;
import com.example.educationalqualityproject.repository.ClassGroupRepository;
import com.example.educationalqualityproject.repository.CourseRepository;
import com.example.educationalqualityproject.repository.SubjectRepository;
import com.example.educationalqualityproject.repository.TeacherRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class AcademicPlanningService {

    private final AcademicPeriodRepository academicPeriodRepository;
    private final CourseRepository courseRepository;
    private final ClassGroupRepository classGroupRepository;
    private final SubjectRepository subjectRepository;
    private final AcademicScheduleRepository academicScheduleRepository;
    private final TeacherRepository teacherRepository;
    private final ResourcePlanningService resourcePlanningService;

    public AcademicPlanningService(AcademicPeriodRepository academicPeriodRepository,
                                   CourseRepository courseRepository,
                                   ClassGroupRepository classGroupRepository,
                                   SubjectRepository subjectRepository,
                                   AcademicScheduleRepository academicScheduleRepository,
                                   TeacherRepository teacherRepository,
                                   ResourcePlanningService resourcePlanningService) {
        this.academicPeriodRepository = academicPeriodRepository;
        this.courseRepository = courseRepository;
        this.classGroupRepository = classGroupRepository;
        this.subjectRepository = subjectRepository;
        this.academicScheduleRepository = academicScheduleRepository;
        this.teacherRepository = teacherRepository;
        this.resourcePlanningService = resourcePlanningService;
    }

    public List<AcademicPeriod> getAcademicPeriods() {
        return academicPeriodRepository.findAll();
    }

    public AcademicPeriod getAcademicPeriod(String id) {
        return findOrNotFound(academicPeriodRepository.findById(id), "Período letivo não encontrado");
    }

    public AcademicPeriod saveAcademicPeriod(String id, AcademicPeriod period) {
        requireText(period.getName(), "Nome do período letivo é obrigatório");
        if (period.getStartsOn() == null || period.getEndsOn() == null
                || period.getEndsOn().isBefore(period.getStartsOn())) {
            throw badRequest("O período deve ter datas de início e fim válidas");
        }
        if (id != null) {
            getAcademicPeriod(id);
            period.setId(id);
        }
        return academicPeriodRepository.save(period);
    }

    public void deleteAcademicPeriod(String id) {
        getAcademicPeriod(id);
        boolean usedByClass = classGroupRepository.findAll().stream()
                .anyMatch(classGroup -> id.equals(classGroup.getAcademicPeriodId()));
        boolean usedBySchedule = academicScheduleRepository.findAll().stream()
                .anyMatch(schedule -> id.equals(schedule.getAcademicPeriodId()));
        if (usedByClass || usedBySchedule) {
            throw conflict("Não é possível excluir um período vinculado a turmas ou grades");
        }
        academicPeriodRepository.deleteById(id);
    }

    public List<Course> getCourses() {
        return courseRepository.findAll();
    }

    public Course getCourse(String id) {
        return findOrNotFound(courseRepository.findById(id), "Curso não encontrado");
    }

    public Course saveCourse(String id, Course course) {
        requireText(course.getCode(), "Código do curso é obrigatório");
        requireText(course.getName(), "Nome do curso é obrigatório");
        if (id != null) {
            getCourse(id);
            course.setId(id);
        }
        return courseRepository.save(course);
    }

    public void deleteCourse(String id) {
        getCourse(id);
        boolean usedByClass = classGroupRepository.findAll().stream()
                .anyMatch(classGroup -> id.equals(classGroup.getCourseId()));
        boolean usedBySubject = subjectRepository.findAll().stream()
                .anyMatch(subject -> id.equals(subject.getCourseId()));
        if (usedByClass || usedBySubject) {
            throw conflict("Não é possível excluir um curso vinculado a turmas ou disciplinas");
        }
        courseRepository.deleteById(id);
    }

    public List<ClassGroup> getClassGroups() {
        return classGroupRepository.findAll();
    }

    public ClassGroup getClassGroup(String id) {
        return findOrNotFound(classGroupRepository.findById(id), "Turma não encontrada");
    }

    public ClassGroup saveClassGroup(String id, ClassGroup classGroup) {
        requireText(classGroup.getCode(), "Código da turma é obrigatório");
        requireText(classGroup.getName(), "Nome da turma é obrigatório");
        requireText(classGroup.getCourseId(), "Curso da turma é obrigatório");
        requireText(classGroup.getAcademicPeriodId(), "Período letivo da turma é obrigatório");
        requireReference(courseRepository.findById(classGroup.getCourseId()).isPresent(), "Curso");
        requireReference(academicPeriodRepository.findById(classGroup.getAcademicPeriodId()).isPresent(), "Período letivo");
        if (id != null) {
            getClassGroup(id);
            classGroup.setId(id);
        }
        return classGroupRepository.save(classGroup);
    }

    public void deleteClassGroup(String id) {
        getClassGroup(id);
        boolean usedBySchedule = academicScheduleRepository.findAll().stream()
            .filter(schedule -> schedule.getActivities() != null)
                .flatMap(schedule -> schedule.getActivities().stream())
                .anyMatch(activity -> id.equals(activity.getClassId()));
        if (usedBySchedule) {
            throw conflict("Não é possível excluir uma turma usada por uma atividade de grade");
        }
        classGroupRepository.deleteById(id);
    }

    public List<Subject> getSubjects() {
        return subjectRepository.findAll();
    }

    public Subject getSubject(String id) {
        return findOrNotFound(subjectRepository.findById(id), "Disciplina não encontrada");
    }

    public Subject saveSubject(String id, Subject subject) {
        requireText(subject.getCode(), "Código da disciplina é obrigatório");
        requireText(subject.getName(), "Nome da disciplina é obrigatório");
        requireText(subject.getCourseId(), "Curso da disciplina é obrigatório");
        requireReference(courseRepository.findById(subject.getCourseId()).isPresent(), "Curso");
        if (id != null) {
            getSubject(id);
            subject.setId(id);
        }
        return subjectRepository.save(subject);
    }

    public void deleteSubject(String id) {
        getSubject(id);
        boolean usedBySchedule = academicScheduleRepository.findAll().stream()
            .filter(schedule -> schedule.getActivities() != null)
                .flatMap(schedule -> schedule.getActivities().stream())
                .anyMatch(activity -> id.equals(activity.getSubjectId()));
        if (usedBySchedule) {
            throw conflict("Não é possível excluir uma disciplina usada por uma atividade de grade");
        }
        subjectRepository.deleteById(id);
    }

    public List<AcademicSchedule> getAcademicSchedules() {
        return academicScheduleRepository.findAll();
    }

    public AcademicSchedule getAcademicSchedule(String id) {
        return findOrNotFound(academicScheduleRepository.findById(id), "Grade não encontrada");
    }

    public AcademicSchedule saveAcademicSchedule(String id, AcademicSchedule schedule) {
        return saveAcademicSchedule(id, schedule, "sistema", null);
    }

    public AcademicSchedule saveAcademicSchedule(String id, AcademicSchedule schedule,
                                                 String actor, Integer expectedVersion) {
        requireText(schedule.getName(), "Nome da grade é obrigatório");
        requireText(schedule.getAcademicPeriodId(), "Período letivo da grade é obrigatório");
        requireText(actor, "Identificador do responsável é obrigatório");
        AcademicPeriod period = academicPeriodRepository.findById(schedule.getAcademicPeriodId())
                .orElseThrow(() -> badRequest("Período letivo informado não existe"));
        if (schedule.getActivities() == null) {
            throw badRequest("A lista de atividades da grade não pode ser nula");
        }
        AcademicSchedule existing = null;
        if (id != null) {
            existing = getAcademicSchedule(id);
            initializeWorkflow(existing);
            if ("PENDENTE_APROVACAO".equals(existing.getStatus())) {
                throw conflict("A grade não pode ser editada enquanto aguarda aprovação");
            }
            if (expectedVersion == null) {
                throw new ResponseStatusException(HttpStatus.PRECONDITION_REQUIRED,
                        "Informe expectedVersion para evitar sobrescrever uma versão mais recente");
            }
            if (expectedVersion != existing.getVersionNumber()) {
                throw conflict("A grade foi alterada desde a versão consultada; atualize antes de salvar");
            }
            schedule.setId(id);
        }
        for (AcademicSchedule.ScheduledActivity activity : schedule.getActivities()) {
            validateActivity(period, activity);
        }
        resourcePlanningService.validateScheduleAllocations(schedule);
        schedule.setStatus("RASCUNHO");
        initializeWorkflow(schedule);
        if (existing == null) {
            schedule.setVersions(new ArrayList<>());
            schedule.setAuditEvents(new ArrayList<>());
            schedule.setAdjustmentRequests(new ArrayList<>());
            schedule.setNotifications(new ArrayList<>());
            schedule.setVersionNumber(1);
            addAuditEvent(schedule, "GRADE_CRIADA", actor, null, "RASCUNHO", null);
        } else {
            schedule.setVersions(existing.getVersions());
            schedule.setAuditEvents(existing.getAuditEvents());
            schedule.setAdjustmentRequests(existing.getAdjustmentRequests());
            schedule.setNotifications(existing.getNotifications());
            schedule.setPublishedVersionNumber(existing.getPublishedVersionNumber());
            schedule.setPublishedAt(existing.getPublishedAt());
            schedule.setVersionNumber(existing.getVersionNumber() + 1);
            addAuditEvent(schedule, "VERSAO_CRIADA", actor, existing.getStatus(), "RASCUNHO",
                    "Versão " + schedule.getVersionNumber());
        }
        schedule.getVersions().add(createVersion(schedule, actor));
        return academicScheduleRepository.save(schedule);
    }

    public AcademicSchedule submitScheduleForApproval(String id, String actor) {
        AcademicSchedule schedule = getAcademicSchedule(id);
        requireText(actor, "Identificador do responsável é obrigatório");
        requireStatus(schedule, "RASCUNHO", "Somente uma grade em rascunho pode ser submetida");
        changeStatus(schedule, "SUBMETIDA_PARA_APROVACAO", actor, "PENDENTE_APROVACAO", null);
        notifyRecipient(schedule, currentVersion(schedule).getCreatedBy(),
                "A versão " + schedule.getVersionNumber() + " da grade foi submetida para aprovação.");
        return academicScheduleRepository.save(schedule);
    }

    public AcademicSchedule approveSchedule(String id, String actor) {
        AcademicSchedule schedule = getAcademicSchedule(id);
        requireText(actor, "Identificador do responsável é obrigatório");
        requireStatus(schedule, "PENDENTE_APROVACAO", "Somente uma grade submetida pode ser aprovada");
        changeStatus(schedule, "GRADE_APROVADA", actor, "APROVADA", null);
        notifyRecipient(schedule, currentVersion(schedule).getCreatedBy(),
                "A versão " + schedule.getVersionNumber() + " da grade foi aprovada.");
        return academicScheduleRepository.save(schedule);
    }

    public AcademicSchedule rejectSchedule(String id, String actor, String justification) {
        AcademicSchedule schedule = getAcademicSchedule(id);
        requireText(actor, "Identificador do responsável é obrigatório");
        requireText(justification, "A justificativa da rejeição é obrigatória");
        requireStatus(schedule, "PENDENTE_APROVACAO", "Somente uma grade submetida pode ser rejeitada");
        changeStatus(schedule, "GRADE_REJEITADA", actor, "REJEITADA", justification);
        notifyRecipient(schedule, currentVersion(schedule).getCreatedBy(),
                "A versão " + schedule.getVersionNumber() + " foi rejeitada: " + justification);
        return academicScheduleRepository.save(schedule);
    }

    public AcademicSchedule publishSchedule(String id, String actor) {
        AcademicSchedule schedule = getAcademicSchedule(id);
        requireText(actor, "Identificador do responsável é obrigatório");
        requireStatus(schedule, "APROVADA", "Somente uma grade aprovada pode ser publicada");
        Integer previousPublishedVersion = schedule.getPublishedVersionNumber();
        changeStatus(schedule, "GRADE_PUBLICADA", actor, "PUBLICADA", null);
        schedule.setPublishedVersionNumber(schedule.getVersionNumber());
        schedule.setPublishedAt(LocalDateTime.now());
        Set<String> recipients = new LinkedHashSet<>();
        addTeacherRecipients(recipients, currentVersion(schedule).getActivities());
        if (previousPublishedVersion != null) {
            addTeacherRecipients(recipients, findVersion(schedule, previousPublishedVersion).getActivities());
        }
        for (String recipient : recipients) {
            notifyRecipient(schedule, recipient,
                    "A versão " + schedule.getVersionNumber() + " da grade foi publicada.");
        }
        return academicScheduleRepository.save(schedule);
    }

    public List<AcademicSchedule.ScheduleVersion> getScheduleVersions(String id) {
        AcademicSchedule schedule = getAcademicSchedule(id);
        initializeWorkflow(schedule);
        return schedule.getVersions();
    }

    public AcademicSchedule.ScheduleVersion getPublishedScheduleVersion(String id) {
        AcademicSchedule schedule = getAcademicSchedule(id);
        if (schedule.getPublishedVersionNumber() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "A grade ainda não foi publicada");
        }
        return findVersion(schedule, schedule.getPublishedVersionNumber());
    }

    public List<AcademicSchedule.ScheduleChange> compareScheduleVersions(String id, int from, int to) {
        AcademicSchedule schedule = getAcademicSchedule(id);
        AcademicSchedule.ScheduleVersion previous = findVersion(schedule, from);
        AcademicSchedule.ScheduleVersion next = findVersion(schedule, to);
        Map<String, AcademicSchedule.ScheduledActivity> previousActivities = indexActivities(previous.getActivities());
        Map<String, AcademicSchedule.ScheduledActivity> nextActivities = indexActivities(next.getActivities());
        List<AcademicSchedule.ScheduleChange> changes = new ArrayList<>();
        for (Map.Entry<String, AcademicSchedule.ScheduledActivity> entry : previousActivities.entrySet()) {
            AcademicSchedule.ScheduledActivity newer = nextActivities.get(entry.getKey());
            if (newer == null) {
                changes.add(new AcademicSchedule.ScheduleChange("REMOVIDA", entry.getKey(), entry.getValue(), null));
            } else if (!sameActivity(entry.getValue(), newer)) {
                changes.add(new AcademicSchedule.ScheduleChange("ALTERADA", entry.getKey(), entry.getValue(), newer));
            }
        }
        for (Map.Entry<String, AcademicSchedule.ScheduledActivity> entry : nextActivities.entrySet()) {
            if (!previousActivities.containsKey(entry.getKey())) {
                changes.add(new AcademicSchedule.ScheduleChange("INCLUIDA", entry.getKey(), null, entry.getValue()));
            }
        }
        return changes;
    }

    public AcademicSchedule.AdjustmentRequest requestScheduleAdjustment(
            String id, String requester, String activityId, String description) {
        AcademicSchedule schedule = getAcademicSchedule(id);
        requireText(requester, "Identificador do solicitante é obrigatório");
        requireText(description, "Descrição do ajuste é obrigatória");
        initializeWorkflow(schedule);
        if (activityId != null && schedule.getActivities().stream()
                .noneMatch(activity -> activityId.equals(activity.getId()))) {
            throw badRequest("Atividade informada não pertence à grade");
        }
        AcademicSchedule.AdjustmentRequest request = new AcademicSchedule.AdjustmentRequest();
        request.setRequester(requester);
        request.setActivityId(activityId);
        request.setDescription(description);
        request.setCreatedAt(LocalDateTime.now());
        schedule.getAdjustmentRequests().add(request);
        addAuditEvent(schedule, "AJUSTE_SOLICITADO", requester, schedule.getStatus(), schedule.getStatus(), request.getId());
        AcademicSchedule.ScheduleVersion version = currentVersion(schedule);
        if (version.getCreatedBy() != null && !version.getCreatedBy().equals(requester)) {
            notifyRecipient(schedule, version.getCreatedBy(), "Nova solicitação de ajuste na grade: " + description);
        }
        academicScheduleRepository.save(schedule);
        return request;
    }

    public AcademicSchedule.AdjustmentRequest respondToAdjustment(String id, String requestId, String actor,
                                                                  String decision, String response) {
        AcademicSchedule schedule = getAcademicSchedule(id);
        initializeWorkflow(schedule);
        requireText(actor, "Identificador do responsável é obrigatório");
        requireText(response, "A resposta da solicitação é obrigatória");
        if (!"ACEITA".equals(decision) && !"REJEITADA".equals(decision)) {
            throw badRequest("A decisão deve ser ACEITA ou REJEITADA");
        }
        AcademicSchedule.AdjustmentRequest request = schedule.getAdjustmentRequests().stream()
                .filter(item -> requestId.equals(item.getId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitação de ajuste não encontrada"));
        if (!"ABERTA".equals(request.getStatus())) {
            throw conflict("A solicitação de ajuste já foi respondida");
        }
        request.setStatus(decision);
        request.setResponse(response);
        request.setRespondedBy(actor);
        request.setRespondedAt(LocalDateTime.now());
        addAuditEvent(schedule, "AJUSTE_" + decision, actor, schedule.getStatus(), schedule.getStatus(), requestId);
        notifyRecipient(schedule, request.getRequester(), "Sua solicitação de ajuste foi " + decision.toLowerCase() + ".");
        academicScheduleRepository.save(schedule);
        return request;
    }

    public List<AcademicSchedule.AdjustmentRequest> getScheduleAdjustmentRequests(String id) {
        AcademicSchedule schedule = getAcademicSchedule(id);
        initializeWorkflow(schedule);
        return schedule.getAdjustmentRequests();
    }

    public List<AcademicSchedule.ScheduleNotification> getScheduleNotifications(String id, String recipient) {
        AcademicSchedule schedule = getAcademicSchedule(id);
        requireText(recipient, "Identificador do destinatário é obrigatório");
        initializeWorkflow(schedule);
        return schedule.getNotifications().stream()
                .filter(notification -> recipient.equals(notification.getRecipient()))
                .toList();
    }

    public AcademicSchedule markNotificationRead(String id, String notificationId, String recipient) {
        AcademicSchedule schedule = getAcademicSchedule(id);
        requireText(recipient, "Identificador do destinatário é obrigatório");
        initializeWorkflow(schedule);
        AcademicSchedule.ScheduleNotification notification = schedule.getNotifications().stream()
                .filter(item -> notificationId.equals(item.getId()) && recipient.equals(item.getRecipient()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notificação não encontrada"));
        notification.setRead(true);
        return academicScheduleRepository.save(schedule);
    }

    public List<AcademicSchedule.ScheduleAuditEvent> getScheduleAudit(String id) {
        AcademicSchedule schedule = getAcademicSchedule(id);
        initializeWorkflow(schedule);
        return schedule.getAuditEvents();
    }

    public List<AcademicSchedule.ScheduleReport> getScheduleReport(String academicPeriodId, String status,
                                                                   String teacherId, String classId,
                                                                   String roomResourceId) {
        List<AcademicSchedule.ScheduleReport> report = new ArrayList<>();
        boolean filterActivities = teacherId != null || classId != null || roomResourceId != null;
        for (AcademicSchedule schedule : academicScheduleRepository.findAll()) {
            if (academicPeriodId != null && !academicPeriodId.equals(schedule.getAcademicPeriodId())) {
                continue;
            }
            if (status != null && !status.equals(schedule.getStatus())) {
                continue;
            }
                List<AcademicSchedule.ScheduledActivity> scheduleActivities = schedule.getActivities() == null
                    ? List.of() : schedule.getActivities();
                List<AcademicSchedule.ScheduledActivity> activities = scheduleActivities.stream()
                    .filter(activity -> teacherId == null || activity.getTeacherIds() != null
                        && activity.getTeacherIds().contains(teacherId))
                    .filter(activity -> classId == null || classId.equals(activity.getClassId()))
                    .filter(activity -> roomResourceId == null || roomResourceId.equals(activity.getRoomResourceId()))
                    .toList();
            if (filterActivities && activities.isEmpty()) {
                continue;
            }
            AcademicSchedule.ScheduleReport row = new AcademicSchedule.ScheduleReport();
            row.setScheduleId(schedule.getId());
            row.setName(schedule.getName());
            row.setAcademicPeriodId(schedule.getAcademicPeriodId());
            row.setStatus(schedule.getStatus());
            row.setVersionNumber(schedule.getVersionNumber());
            row.setPublishedVersionNumber(schedule.getPublishedVersionNumber());
            row.setPublishedAt(schedule.getPublishedAt());
            row.setActivities(filterActivities ? activities : scheduleActivities);
            report.add(row);
        }
        return report;
    }

    public void deleteAcademicSchedule(String id) {
        AcademicSchedule schedule = getAcademicSchedule(id);
        if (schedule.getPublishedVersionNumber() != null) {
            throw conflict("Grades publicadas não podem ser excluídas para preservar o histórico");
        }
        academicScheduleRepository.deleteById(id);
    }

    private void initializeWorkflow(AcademicSchedule schedule) {
        if (schedule.getVersions() == null) {
            schedule.setVersions(new ArrayList<>());
        }
        if (schedule.getAuditEvents() == null) {
            schedule.setAuditEvents(new ArrayList<>());
        }
        if (schedule.getAdjustmentRequests() == null) {
            schedule.setAdjustmentRequests(new ArrayList<>());
        }
        if (schedule.getNotifications() == null) {
            schedule.setNotifications(new ArrayList<>());
        }
        if (schedule.getActivities() == null) {
            schedule.setActivities(new ArrayList<>());
        }
    }

    private AcademicSchedule.ScheduleVersion createVersion(AcademicSchedule schedule, String actor) {
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

    private AcademicSchedule.ScheduleVersion currentVersion(AcademicSchedule schedule) {
        if (schedule.getVersions().isEmpty()) {
            AcademicSchedule.ScheduleVersion initial = createVersion(schedule,
                    schedule.getStatus().equals("RASCUNHO") ? "sistema" : "desconhecido");
            initial.setNumber(Math.max(1, schedule.getVersionNumber()));
            schedule.setVersionNumber(initial.getNumber());
            schedule.getVersions().add(initial);
        }
        return findVersion(schedule, schedule.getVersionNumber());
    }

    private AcademicSchedule.ScheduleVersion findVersion(AcademicSchedule schedule, int number) {
        return schedule.getVersions().stream()
                .filter(version -> version.getNumber() == number)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Versão " + number + " da grade não encontrada"));
    }

    private Map<String, AcademicSchedule.ScheduledActivity> indexActivities(
            List<AcademicSchedule.ScheduledActivity> activities) {
        Map<String, AcademicSchedule.ScheduledActivity> indexed = new HashMap<>();
        for (AcademicSchedule.ScheduledActivity activity : activities) {
            indexed.put(activity.getId(), activity);
        }
        return indexed;
    }

    private boolean sameActivity(AcademicSchedule.ScheduledActivity left,
                                 AcademicSchedule.ScheduledActivity right) {
        if (!Objects.equals(left.getCourseId(), right.getCourseId())
                || !Objects.equals(left.getClassId(), right.getClassId())
                || !Objects.equals(left.getSubjectId(), right.getSubjectId())
                || !Objects.equals(left.getTeacherIds(), right.getTeacherIds())
                || !Objects.equals(left.getStartsAt(), right.getStartsAt())
                || !Objects.equals(left.getEndsAt(), right.getEndsAt())
                || !Objects.equals(left.getRequiredRoomCapacity(), right.getRequiredRoomCapacity())
                || !Objects.equals(left.getRequiredRoomFeatures(), right.getRequiredRoomFeatures())
                || !Objects.equals(left.getRoomResourceId(), right.getRoomResourceId())) {
            return false;
        }
        List<AcademicSchedule.MaterialAssignment> leftMaterials = left.getMaterials();
        List<AcademicSchedule.MaterialAssignment> rightMaterials = right.getMaterials();
        if (leftMaterials == null || rightMaterials == null) {
            return leftMaterials == rightMaterials;
        }
        if (leftMaterials.size() != rightMaterials.size()) {
            return false;
        }
        for (int index = 0; index < leftMaterials.size(); index++) {
            AcademicSchedule.MaterialAssignment leftMaterial = leftMaterials.get(index);
            AcademicSchedule.MaterialAssignment rightMaterial = rightMaterials.get(index);
            if (!Objects.equals(leftMaterial.getResourceId(), rightMaterial.getResourceId())
                    || leftMaterial.getQuantity() != rightMaterial.getQuantity()) {
                return false;
            }
        }
        return true;
    }

    private void requireStatus(AcademicSchedule schedule, String expected, String message) {
        if (!expected.equals(schedule.getStatus())) {
            throw conflict(message);
        }
    }

    private void changeStatus(AcademicSchedule schedule, String action, String actor,
                              String newStatus, String details) {
        initializeWorkflow(schedule);
        currentVersion(schedule);
        String previousStatus = schedule.getStatus();
        schedule.setStatus(newStatus);
        addAuditEvent(schedule, action, actor, previousStatus, newStatus, details);
    }

    private void addAuditEvent(AcademicSchedule schedule, String action, String actor,
                               String previousStatus, String newStatus, String details) {
        initializeWorkflow(schedule);
        AcademicSchedule.ScheduleAuditEvent event = new AcademicSchedule.ScheduleAuditEvent();
        event.setAction(action);
        event.setActor(actor);
        event.setPreviousStatus(previousStatus);
        event.setNewStatus(newStatus);
        event.setDetails(details);
        event.setOccurredAt(LocalDateTime.now());
        schedule.getAuditEvents().add(event);
    }

    private void notifyRecipient(AcademicSchedule schedule, String recipient, String message) {
        if (recipient == null || recipient.isBlank()) {
            return;
        }
        initializeWorkflow(schedule);
        AcademicSchedule.ScheduleNotification notification = new AcademicSchedule.ScheduleNotification();
        notification.setRecipient(recipient);
        notification.setMessage(message);
        notification.setCreatedAt(LocalDateTime.now());
        schedule.getNotifications().add(notification);
    }

    private void addTeacherRecipients(Set<String> recipients,
                                      List<AcademicSchedule.ScheduledActivity> activities) {
        for (AcademicSchedule.ScheduledActivity activity : activities) {
            if (activity.getTeacherIds() != null) {
                recipients.addAll(activity.getTeacherIds());
            }
        }
    }

    private void validateActivity(AcademicPeriod period, AcademicSchedule.ScheduledActivity activity) {
        if (activity == null || activity.getStartsAt() == null || activity.getEndsAt() == null
                || !activity.getEndsAt().isAfter(activity.getStartsAt())) {
            throw badRequest("Cada atividade deve ter um intervalo válido");
        }
        requireText(activity.getCourseId(), "Curso da atividade é obrigatório");
        requireText(activity.getClassId(), "Turma da atividade é obrigatória");
        requireText(activity.getSubjectId(), "Disciplina da atividade é obrigatória");
        if (activity.getStartsAt().toLocalDate().isBefore(period.getStartsOn())
                || activity.getEndsAt().toLocalDate().isAfter(period.getEndsOn())) {
            throw badRequest("A atividade deve ocorrer dentro do período letivo da grade");
        }

        ClassGroup classGroup = classGroupRepository.findById(activity.getClassId())
                .orElseThrow(() -> badRequest("Turma informada não existe"));
        Subject subject = subjectRepository.findById(activity.getSubjectId())
                .orElseThrow(() -> badRequest("Disciplina informada não existe"));
        requireReference(courseRepository.findById(activity.getCourseId()).isPresent(), "Curso");
        if (!activity.getCourseId().equals(classGroup.getCourseId())
                || !period.getId().equals(classGroup.getAcademicPeriodId())
                || !activity.getCourseId().equals(subject.getCourseId())) {
            throw badRequest("Curso, turma, disciplina e período letivo não são compatíveis");
        }
        if (activity.getTeacherIds() == null || activity.getTeacherIds().isEmpty()) {
            throw badRequest("A atividade deve ter ao menos um professor associado");
        }
        for (String teacherId : activity.getTeacherIds()) {
            requireText(teacherId, "Identificador do professor é obrigatório");
            requireReference(teacherRepository.findById(teacherId).isPresent(), "Professor");
        }
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

    private <T> T findOrNotFound(java.util.Optional<T> entity, String message) {
        return entity.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, message));
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }
}