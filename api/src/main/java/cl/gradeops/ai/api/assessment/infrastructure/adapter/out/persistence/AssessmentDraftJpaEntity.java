package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "assessment_drafts")
@Getter
@Setter
@NoArgsConstructor
public class AssessmentDraftJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "assessment_id", nullable = false)
    private UUID assessmentId;

    @Column(name = "version_number", nullable = false)
    private int versionNumber;

    @Column(name = "previous_version_id")
    private UUID previousVersionId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "context", nullable = false)
    private String context;

    @Column(name = "instructions", nullable = false)
    private String instructions;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "objectives", nullable = false, columnDefinition = "jsonb")
    private List<String> objectives;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "deliverables", nullable = false, columnDefinition = "jsonb")
    private List<String> deliverables;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "constraints", nullable = false, columnDefinition = "jsonb")
    private List<String> constraints;

    @Column(name = "agent_execution_log_id")
    private UUID agentExecutionLogId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
