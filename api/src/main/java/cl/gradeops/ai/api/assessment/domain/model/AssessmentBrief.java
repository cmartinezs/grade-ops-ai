package cl.gradeops.ai.api.assessment.domain.model;

import cl.gradeops.ai.api.shared.domain.exception.DomainInvariantViolationException;
import cl.gradeops.ai.api.shared.domain.model.AggregateRoot;

import java.time.Instant;
import java.util.UUID;

public class AssessmentBrief extends AggregateRoot<UUID> {

    private UUID id;
    private AssessmentId assessmentId;
    private String learningGoal;
    private String topic;
    private String level;
    private String duration;
    private String language;
    private Instant createdAt;

    private AssessmentBrief() {}

    public static AssessmentBrief create(AssessmentId assessmentId, String learningGoal, String topic,
                                          String level, String duration, String language) {
        validate(assessmentId, learningGoal, topic, level, duration, language);
        AssessmentBrief b = new AssessmentBrief();
        b.id = UUID.randomUUID();
        b.assessmentId = assessmentId;
        b.learningGoal = learningGoal;
        b.topic = topic;
        b.level = level;
        b.duration = duration;
        b.language = language;
        b.createdAt = Instant.now();
        return b;
    }

    public static AssessmentBrief restore(UUID id, AssessmentId assessmentId, String learningGoal, String topic,
                                           String level, String duration, String language, Instant createdAt) {
        if (id == null) throw new DomainInvariantViolationException("id must not be null");
        validate(assessmentId, learningGoal, topic, level, duration, language);
        if (createdAt == null) throw new DomainInvariantViolationException("createdAt must not be null");
        AssessmentBrief b = new AssessmentBrief();
        b.id = id;
        b.assessmentId = assessmentId;
        b.learningGoal = learningGoal;
        b.topic = topic;
        b.level = level;
        b.duration = duration;
        b.language = language;
        b.createdAt = createdAt;
        return b;
    }

    private static void validate(AssessmentId assessmentId, String learningGoal, String topic,
                                  String level, String duration, String language) {
        if (assessmentId == null) throw new DomainInvariantViolationException("assessmentId must not be null");
        if (learningGoal == null || learningGoal.isBlank()) throw new DomainInvariantViolationException("learningGoal must not be blank");
        if (topic == null || topic.isBlank())               throw new DomainInvariantViolationException("topic must not be blank");
        if (level == null || level.isBlank())               throw new DomainInvariantViolationException("level must not be blank");
        if (duration == null || duration.isBlank())         throw new DomainInvariantViolationException("duration must not be blank");
        if (language == null || language.isBlank())         throw new DomainInvariantViolationException("language must not be blank");
    }

    @Override protected UUID id()          { return id; }
    public UUID getId()                    { return id; }
    public AssessmentId getAssessmentId()  { return assessmentId; }
    public String getLearningGoal()        { return learningGoal; }
    public String getTopic()               { return topic; }
    public String getLevel()               { return level; }
    public String getDuration()            { return duration; }
    public String getLanguage()            { return language; }
    public Instant getCreatedAt()          { return createdAt; }
}
