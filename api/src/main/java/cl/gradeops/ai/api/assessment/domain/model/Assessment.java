package cl.gradeops.ai.api.assessment.domain.model;

import cl.gradeops.ai.api.shared.domain.exception.DomainInvariantViolationException;
import cl.gradeops.ai.api.shared.domain.model.AggregateRoot;

import java.time.Instant;
import java.util.UUID;

public class Assessment extends AggregateRoot<AssessmentId> {

    private AssessmentId id;
    private String teacherUid;
    private AssessmentStatus status;
    private Instant createdAt;

    private Assessment() {}

    public static Assessment create(String teacherUid) {
        if (teacherUid == null || teacherUid.isBlank()) throw new DomainInvariantViolationException("teacherUid must not be blank");
        Assessment a = new Assessment();
        a.id = new AssessmentId(UUID.randomUUID());
        a.teacherUid = teacherUid;
        a.status = AssessmentStatus.DRAFT;
        a.createdAt = Instant.now();
        return a;
    }

    public static Assessment restore(AssessmentId id, String teacherUid, AssessmentStatus status, Instant createdAt) {
        if (id == null)                                 throw new DomainInvariantViolationException("id must not be null");
        if (teacherUid == null || teacherUid.isBlank()) throw new DomainInvariantViolationException("teacherUid must not be blank");
        if (status == null)                             throw new DomainInvariantViolationException("status must not be null");
        if (createdAt == null)                          throw new DomainInvariantViolationException("createdAt must not be null");
        Assessment a = new Assessment();
        a.id = id;
        a.teacherUid = teacherUid;
        a.status = status;
        a.createdAt = createdAt;
        return a;
    }

    @Override protected AssessmentId id() { return id; }
    public AssessmentId getId()           { return id; }
    public String getTeacherUid()         { return teacherUid; }
    public AssessmentStatus getStatus()   { return status; }
    public Instant getCreatedAt()         { return createdAt; }
}
