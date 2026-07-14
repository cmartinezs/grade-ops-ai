package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "assessment_briefs")
@Getter
@Setter
@NoArgsConstructor
public class AssessmentBriefJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "assessment_id", nullable = false, unique = true)
    private UUID assessmentId;

    @Column(name = "learning_goal", nullable = false)
    private String learningGoal;

    @Column(name = "topic", nullable = false)
    private String topic;

    @Column(name = "level", nullable = false)
    private String level;

    @Column(name = "duration", nullable = false)
    private String duration;

    @Column(name = "language", nullable = false)
    private String language;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
