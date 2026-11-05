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

import java.util.List;

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
        requireText(schedule.getName(), "Nome da grade é obrigatório");
        requireText(schedule.getAcademicPeriodId(), "Período letivo da grade é obrigatório");
        AcademicPeriod period = academicPeriodRepository.findById(schedule.getAcademicPeriodId())
                .orElseThrow(() -> badRequest("Período letivo informado não existe"));
        if (schedule.getActivities() == null) {
            throw badRequest("A lista de atividades da grade não pode ser nula");
        }
        if (id != null) {
            getAcademicSchedule(id);
            schedule.setId(id);
        }
        for (AcademicSchedule.ScheduledActivity activity : schedule.getActivities()) {
            validateActivity(period, activity);
        }
        resourcePlanningService.validateScheduleAllocations(schedule);
        schedule.setStatus("RASCUNHO");
        return academicScheduleRepository.save(schedule);
    }

    public void deleteAcademicSchedule(String id) {
        getAcademicSchedule(id);
        academicScheduleRepository.deleteById(id);
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