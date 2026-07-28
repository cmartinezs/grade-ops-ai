package cl.gradeops.ai.api.assessment.domain.model;

import cl.gradeops.ai.api.shared.domain.exception.DomainInvariantViolationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class AgentAttemptTest {

    AgentAttempt dispatchFirstAttempt() {
        return AgentAttempt.dispatch(UUID.randomUUID(), 1, "assessment-agent", "v1", "corr-1");
    }

    @Test
    void shouldStartInDispatchedStatusWithNoResolvedProviderOrModelWhenDispatched() {
        UUID aiOperationId = UUID.randomUUID();
        AgentAttempt a = AgentAttempt.dispatch(aiOperationId, 1, "assessment-agent", "v1", "corr-1");

        assertThat(a.getId()).isNotNull();
        assertThat(a.getAiOperationId()).isEqualTo(aiOperationId);
        assertThat(a.getAttemptNumber()).isEqualTo(1);
        assertThat(a.getAgentName()).isEqualTo("assessment-agent");
        assertThat(a.getPromptVersion()).isEqualTo("v1");
        assertThat(a.getCorrelationId()).isEqualTo("corr-1");
        assertThat(a.getResolvedProvider()).isNull();
        assertThat(a.getResolvedModel()).isNull();
        assertThat(a.getStatus()).isEqualTo(AgentAttemptStatus.DISPATCHED);
        assertThat(a.getDispatchedAt()).isNotNull();
        assertThat(a.getCompletedAt()).isNull();
        assertThat(a.getFailureCode()).isNull();
        assertThat(a.getStructuredResult()).isNull();
    }

    @Test
    void shouldRejectNullAiOperationIdWhenDispatching() {
        assertThatThrownBy(() -> AgentAttempt.dispatch(null, 1, "assessment-agent", "v1", "corr-1"))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("aiOperationId");
    }

    @Test
    void shouldRejectAttemptNumberLessThanOneWhenDispatching() {
        assertThatThrownBy(() -> AgentAttempt.dispatch(UUID.randomUUID(), 0, "assessment-agent", "v1", "corr-1"))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("attemptNumber");
    }

    @Test
    void shouldRejectBlankAgentNameWhenDispatching() {
        assertThatThrownBy(() -> AgentAttempt.dispatch(UUID.randomUUID(), 1, " ", "v1", "corr-1"))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("agentName");
    }

    @Test
    void shouldTransitionFromDispatchedToCompletedWithResolvedProviderAndModel() {
        AgentAttempt a = dispatchFirstAttempt();

        AgentAttempt completed = a.markCompleted("groq", "llama-3", "req-1", 100, 200,
                new BigDecimal("0.0123"), "{\"title\":\"t\"}");

        assertThat(completed.getStatus()).isEqualTo(AgentAttemptStatus.COMPLETED);
        assertThat(completed.getResolvedProvider()).isEqualTo("groq");
        assertThat(completed.getResolvedModel()).isEqualTo("llama-3");
        assertThat(completed.getProviderRequestId()).isEqualTo("req-1");
        assertThat(completed.getEstimatedInputTokens()).isEqualTo(100);
        assertThat(completed.getEstimatedOutputTokens()).isEqualTo(200);
        assertThat(completed.getCostEstimate()).isEqualByComparingTo("0.0123");
        assertThat(completed.getStructuredResult()).isEqualTo("{\"title\":\"t\"}");
        assertThat(completed.getCompletedAt()).isNotNull();
        assertThat(a.getStatus()).isEqualTo(AgentAttemptStatus.DISPATCHED);
    }

    @Test
    void shouldRejectBlankResolvedProviderWhenMarkingCompleted() {
        AgentAttempt a = dispatchFirstAttempt();

        assertThatThrownBy(() -> a.markCompleted(" ", "llama-3", null, null, null, null, null))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("resolvedProvider");
    }

    @Test
    void shouldRejectBlankResolvedModelWhenMarkingCompleted() {
        AgentAttempt a = dispatchFirstAttempt();

        assertThatThrownBy(() -> a.markCompleted("groq", " ", null, null, null, null, null))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("resolvedModel");
    }

    @Test
    void shouldTransitionFromDispatchedToFailedRetainingStructuredResult() {
        AgentAttempt a = dispatchFirstAttempt();

        AgentAttempt failed = a.markFailed("STALE_ON_COMPLETION", "{\"partial\":true}");

        assertThat(failed.getStatus()).isEqualTo(AgentAttemptStatus.FAILED);
        assertThat(failed.getFailureCode()).isEqualTo("STALE_ON_COMPLETION");
        assertThat(failed.getStructuredResult()).isEqualTo("{\"partial\":true}");
        assertThat(failed.getCompletedAt()).isNotNull();
    }

    @Test
    void shouldRejectBlankFailureCodeWhenMarkingFailed() {
        AgentAttempt a = dispatchFirstAttempt();

        assertThatThrownBy(() -> a.markFailed(" ", null))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("failureCode");
    }

    @Test
    void shouldRejectAnyTransitionFromCompleted() {
        AgentAttempt a = dispatchFirstAttempt().markCompleted("groq", "llama-3", null, null, null, null, null);

        assertThatThrownBy(() -> a.markCompleted("groq", "llama-3", null, null, null, null, null))
                .isInstanceOf(DomainInvariantViolationException.class);
        assertThatThrownBy(() -> a.markFailed("AGENT_ERROR", null))
                .isInstanceOf(DomainInvariantViolationException.class);
    }

    @Test
    void shouldRejectAnyTransitionFromFailed() {
        AgentAttempt a = dispatchFirstAttempt().markFailed("AGENT_ERROR", null);

        assertThatThrownBy(() -> a.markCompleted("groq", "llama-3", null, null, null, null, null))
                .isInstanceOf(DomainInvariantViolationException.class);
        assertThatThrownBy(() -> a.markFailed("AGENT_ERROR", null))
                .isInstanceOf(DomainInvariantViolationException.class);
    }

    @Test
    void shouldRestoreAllFieldsVerbatimWhenRestoringFromPersistence() {
        UUID id = UUID.randomUUID();
        UUID aiOperationId = UUID.randomUUID();
        Instant dispatchedAt = Instant.now().minusSeconds(120);
        Instant completedAt = Instant.now().minusSeconds(60);

        AgentAttempt a = AgentAttempt.restore(id, aiOperationId, 2, "assessment-agent",
                "groq", "llama-3", "v1", "corr-1", dispatchedAt, completedAt,
                AgentAttemptStatus.COMPLETED, "req-1", null, 100, 200,
                new BigDecimal("0.0123"), "{\"title\":\"t\"}");

        assertThat(a.getId()).isEqualTo(id);
        assertThat(a.getAiOperationId()).isEqualTo(aiOperationId);
        assertThat(a.getAttemptNumber()).isEqualTo(2);
        assertThat(a.getAgentName()).isEqualTo("assessment-agent");
        assertThat(a.getResolvedProvider()).isEqualTo("groq");
        assertThat(a.getResolvedModel()).isEqualTo("llama-3");
        assertThat(a.getPromptVersion()).isEqualTo("v1");
        assertThat(a.getCorrelationId()).isEqualTo("corr-1");
        assertThat(a.getDispatchedAt()).isEqualTo(dispatchedAt);
        assertThat(a.getCompletedAt()).isEqualTo(completedAt);
        assertThat(a.getStatus()).isEqualTo(AgentAttemptStatus.COMPLETED);
        assertThat(a.getProviderRequestId()).isEqualTo("req-1");
        assertThat(a.getEstimatedInputTokens()).isEqualTo(100);
        assertThat(a.getEstimatedOutputTokens()).isEqualTo(200);
        assertThat(a.getCostEstimate()).isEqualByComparingTo("0.0123");
        assertThat(a.getStructuredResult()).isEqualTo("{\"title\":\"t\"}");
    }

    @Test
    void shouldRejectNullIdWhenRestoring() {
        assertThatThrownBy(() -> AgentAttempt.restore(null, UUID.randomUUID(), 1, "assessment-agent",
                null, null, "v1", "corr-1", Instant.now(), null,
                AgentAttemptStatus.DISPATCHED, null, null, null, null, null, null))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("id");
    }
}
