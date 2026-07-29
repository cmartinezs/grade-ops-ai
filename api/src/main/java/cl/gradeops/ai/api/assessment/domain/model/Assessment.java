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
    private UUID currentRevisionId;
    private int lockVersion;

    private Assessment() {}

    public static Assessment create(String teacherUid) {
        if (teacherUid == null || teacherUid.isBlank()) throw new DomainInvariantViolationException("teacherUid must not be blank");
        Assessment a = new Assessment();
        a.id = new AssessmentId(UUID.randomUUID());
        a.teacherUid = teacherUid;
        a.status = AssessmentStatus.DRAFT;
        a.createdAt = Instant.now();
        a.currentRevisionId = null;
        a.lockVersion = 0;
        return a;
    }

    /** Legacy 4-arg shape — defaults {@code currentRevisionId=null}/{@code lockVersion=0}. */
    public static Assessment restore(AssessmentId id, String teacherUid, AssessmentStatus status, Instant createdAt) {
        return restore(id, teacherUid, status, createdAt, null, 0);
    }

    public static Assessment restore(AssessmentId id, String teacherUid, AssessmentStatus status, Instant createdAt,
                                      UUID currentRevisionId, int lockVersion) {
        if (id == null)                                 throw new DomainInvariantViolationException("id must not be null");
        if (teacherUid == null || teacherUid.isBlank()) throw new DomainInvariantViolationException("teacherUid must not be blank");
        if (status == null)                             throw new DomainInvariantViolationException("status must not be null");
        if (createdAt == null)                          throw new DomainInvariantViolationException("createdAt must not be null");
        Assessment a = new Assessment();
        a.id = id;
        a.teacherUid = teacherUid;
        a.status = status;
        a.createdAt = createdAt;
        a.currentRevisionId = currentRevisionId;
        a.lockVersion = lockVersion;
        return a;
    }

    /** Used only by the revision-creation use case, under {@code lockVersion} CAS. */
    public Assessment withCurrentRevision(UUID revisionId) {
        if (revisionId == null) throw new DomainInvariantViolationException("revisionId must not be null");
        Assessment a = new Assessment();
        a.id = id;
        a.teacherUid = teacherUid;
        a.status = status;
        a.createdAt = createdAt;
        a.currentRevisionId = revisionId;
        a.lockVersion = lockVersion;
        return a;
    }

    @Override protected AssessmentId id() { return id; }
    public AssessmentId getId()           { return id; }
    public String getTeacherUid()         { return teacherUid; }
    public AssessmentStatus getStatus()   { return status; }
    public Instant getCreatedAt()         { return createdAt; }
    public UUID getCurrentRevisionId()    { return currentRevisionId; }
    public int getLockVersion()           { return lockVersion; }
}
