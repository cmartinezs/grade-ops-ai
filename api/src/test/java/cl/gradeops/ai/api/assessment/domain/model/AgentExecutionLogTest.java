package cl.gradeops.ai.api.assessment.domain.model;

import cl.gradeops.ai.api.shared.domain.exception.DomainInvariantViolationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgentExecutionLogTest {

    private final AssessmentId assessmentId = new AssessmentId(UUID.randomUUID());
    private final Instant startedAt = Instant.now().minusSeconds(2);
    private final Instant finishedAt = Instant.now();

    private AgentExecutionLog successLog() {
        return AgentExecutionLog.create(assessmentId, UUID.randomUUID(), "assessment", "groq", "model-1", "v1",
                "in-hash", "out-hash", 100, 200, 0.01, "COMPLETED", null, startedAt, finishedAt);
    }

    @Test
    void shouldCreateWithNullDraftIdAndGeneratedId() {
        AgentExecutionLog log = successLog();

        assertThat(log.getId()).isNotNull();
        assertThat(log.getDraftId()).isNull();
        assertThat(log.getAssessmentId()).isEqualTo(assessmentId);
        assertThat(log.getStatus()).isEqualTo("COMPLETED");
        assertThat(log.getErrorCode()).isNull();
    }

    @Test
    void withDraftIdShouldReturnNewInstanceWithSameFieldsExceptDraftId() {
        AgentExecutionLog original = successLog();
        UUID draftId = UUID.randomUUID();

        AgentExecutionLog withDraft = original.withDraftId(draftId);

        assertThat(withDraft.getDraftId()).isEqualTo(draftId);
        assertThat(withDraft.getId()).isEqualTo(original.getId());
        assertThat(withDraft.getAssessmentId()).isEqualTo(original.getAssessmentId());
        assertThat(withDraft.getStatus()).isEqualTo(original.getStatus());
        assertThat(original.getDraftId()).isNull();
    }

    @Test
    void shouldAllowNullableFieldsOnFailurePath() {
        AgentExecutionLog log = AgentExecutionLog.create(assessmentId, null, "assessment", null, null, null,
                null, null, null, null, null, "FAILED", "UNREACHABLE", startedAt, finishedAt);

        assertThat(log.getStatus()).isEqualTo("FAILED");
        assertThat(log.getErrorCode()).isEqualTo("UNREACHABLE");
        assertThat(log.getModel()).isNull();
        assertThat(log.getAgentExecutionId()).isNull();
    }

    @Test
    void shouldRejectNullAssessmentId() {
        assertThatThrownBy(() -> AgentExecutionLog.create(null, UUID.randomUUID(), "assessment", null, null,
                null, null, null, null, null, null, "COMPLETED", null, startedAt, finishedAt))
                .isInstanceOf(DomainInvariantViolationException.class);
    }

    @Test
    void shouldRejectBlankStatus() {
        assertThatThrownBy(() -> AgentExecutionLog.create(assessmentId, UUID.randomUUID(), "assessment", null,
                null, null, null, null, null, null, null, " ", null, startedAt, finishedAt))
                .isInstanceOf(DomainInvariantViolationException.class);
    }

    @Test
    void shouldRejectNullStartedAtOrFinishedAt() {
        assertThatThrownBy(() -> AgentExecutionLog.create(assessmentId, UUID.randomUUID(), "assessment", null,
                null, null, null, null, null, null, null, "COMPLETED", null, null, finishedAt))
                .isInstanceOf(DomainInvariantViolationException.class);
        assertThatThrownBy(() -> AgentExecutionLog.create(assessmentId, UUID.randomUUID(), "assessment", null,
                null, null, null, null, null, null, null, "COMPLETED", null, startedAt, null))
                .isInstanceOf(DomainInvariantViolationException.class);
    }

    @Test
    void restoreShouldRejectNullId() {
        assertThatThrownBy(() -> AgentExecutionLog.restore(null, assessmentId, null, UUID.randomUUID(),
                "assessment", null, null, null, null, null, null, null, null, "COMPLETED", null,
                startedAt, finishedAt))
                .isInstanceOf(DomainInvariantViolationException.class);
    }

    @Test
    void restoreShouldReconstructAllFields() {
        UUID id = UUID.randomUUID();
        UUID draftId = UUID.randomUUID();
        UUID agentExecutionId = UUID.randomUUID();

        AgentExecutionLog log = AgentExecutionLog.restore(id, assessmentId, draftId, agentExecutionId,
                "assessment", "groq", "model-1", "v1", "in-hash", "out-hash", 100, 200, 0.01,
                "COMPLETED", null, startedAt, finishedAt);

        assertThat(log.getId()).isEqualTo(id);
        assertThat(log.getDraftId()).isEqualTo(draftId);
        assertThat(log.getAgentExecutionId()).isEqualTo(agentExecutionId);
        assertThat(log.getProvider()).isEqualTo("groq");
        assertThat(log.getModel()).isEqualTo("model-1");
        assertThat(log.getEstimatedInputTokens()).isEqualTo(100);
        assertThat(log.getEstimatedOutputTokens()).isEqualTo(200);
        assertThat(log.getCostEstimate()).isEqualTo(0.01);
        assertThat(log.getStartedAt()).isEqualTo(startedAt);
        assertThat(log.getFinishedAt()).isEqualTo(finishedAt);
    }
}
