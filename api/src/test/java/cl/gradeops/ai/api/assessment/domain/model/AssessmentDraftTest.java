package cl.gradeops.ai.api.assessment.domain.model;

import cl.gradeops.ai.api.shared.domain.exception.DomainInvariantViolationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class AssessmentDraftTest {

    AssessmentId assessmentId() {
        return new AssessmentId(UUID.randomUUID());
    }

    AssessmentDraft firstVersion() {
        return AssessmentDraft.generate(assessmentId(), "title", "context", "instructions",
                List.of("obj1"), List.of("del1"), List.of("con1"), null);
    }

    @Test
    void shouldSetVersionOneAndNullPreviousVersionWhenGeneratingFirstDraft() {
        AssessmentDraft d = firstVersion();

        assertThat(d.getId()).isNotNull();
        assertThat(d.getVersionNumber()).isEqualTo(1);
        assertThat(d.getPreviousVersionId()).isNull();
        assertThat(d.getTitle()).isEqualTo("title");
        assertThat(d.getContext()).isEqualTo("context");
        assertThat(d.getInstructions()).isEqualTo("instructions");
        assertThat(d.getObjectives()).containsExactly("obj1");
        assertThat(d.getDeliverables()).containsExactly("del1");
        assertThat(d.getConstraints()).containsExactly("con1");
        assertThat(d.getAgentExecutionLogId()).isNull();
        assertThat(d.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldRejectNullAssessmentIdWhenGenerating() {
        assertThatThrownBy(() -> AssessmentDraft.generate(null, "title", "context", "instructions",
                List.of(), List.of(), List.of(), null))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("assessmentId");
    }

    @Test
    void shouldRejectBlankTitleWhenGenerating() {
        assertThatThrownBy(() -> AssessmentDraft.generate(assessmentId(), " ", "context", "instructions",
                List.of(), List.of(), List.of(), null))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("title");
    }

    @Test
    void shouldRejectNullContextWhenGenerating() {
        assertThatThrownBy(() -> AssessmentDraft.generate(assessmentId(), "title", null, "instructions",
                List.of(), List.of(), List.of(), null))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("context");
    }

    @Test
    void shouldRejectBlankInstructionsWhenGenerating() {
        assertThatThrownBy(() -> AssessmentDraft.generate(assessmentId(), "title", "context", "",
                List.of(), List.of(), List.of(), null))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("instructions");
    }

    @Test
    void shouldRejectNullObjectivesWhenGenerating() {
        assertThatThrownBy(() -> AssessmentDraft.generate(assessmentId(), "title", "context", "instructions",
                null, List.of(), List.of(), null))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("objectives");
    }

    @Test
    void shouldRejectNullDeliverablesWhenGenerating() {
        assertThatThrownBy(() -> AssessmentDraft.generate(assessmentId(), "title", "context", "instructions",
                List.of(), null, List.of(), null))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("deliverables");
    }

    @Test
    void shouldRejectNullConstraintsWhenGenerating() {
        assertThatThrownBy(() -> AssessmentDraft.generate(assessmentId(), "title", "context", "instructions",
                List.of(), List.of(), null, null))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("constraints");
    }

    @Test
    void shouldIncrementVersionAndLinkToPreviousWhenRegenerating() {
        AssessmentDraft v1 = firstVersion();

        AssessmentDraft v2 = AssessmentDraft.regenerate(v1, "title2", "context2", "instructions2",
                List.of("obj2"), List.of("del2"), List.of("con2"), null);

        assertThat(v2.getId()).isNotEqualTo(v1.getId());
        assertThat(v2.getVersionNumber()).isEqualTo(2);
        assertThat(v2.getPreviousVersionId()).isEqualTo(v1.getId());
        assertThat(v2.getAssessmentId()).isEqualTo(v1.getAssessmentId());
        assertThat(v2.getTitle()).isEqualTo("title2");
    }

    @Test
    void shouldChainVersionNumbersAcrossMultipleRegenerations() {
        AssessmentDraft v1 = firstVersion();
        AssessmentDraft v2 = AssessmentDraft.regenerate(v1, "t2", "c2", "i2", List.of(), List.of(), List.of(), null);
        AssessmentDraft v3 = AssessmentDraft.regenerate(v2, "t3", "c3", "i3", List.of(), List.of(), List.of(), null);

        assertThat(v3.getVersionNumber()).isEqualTo(3);
        assertThat(v3.getPreviousVersionId()).isEqualTo(v2.getId());
    }

    @Test
    void shouldRejectNullPreviousVersionWhenRegenerating() {
        assertThatThrownBy(() -> AssessmentDraft.regenerate(null, "t", "c", "i", List.of(), List.of(), List.of(), null))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("previousVersion");
    }

    @Test
    void shouldRestoreAllFieldsVerbatimWhenRestoringFromPersistence() {
        UUID id = UUID.randomUUID();
        AssessmentId assessmentId = assessmentId();
        UUID previousVersionId = UUID.randomUUID();
        UUID agentExecutionLogId = UUID.randomUUID();
        Instant createdAt = Instant.now().minusSeconds(60);

        AssessmentDraft d = AssessmentDraft.restore(id, assessmentId, 2, previousVersionId,
                "title", "context", "instructions",
                List.of("obj"), List.of("del"), List.of("con"),
                agentExecutionLogId, createdAt);

        assertThat(d.getId()).isEqualTo(id);
        assertThat(d.getAssessmentId()).isEqualTo(assessmentId);
        assertThat(d.getVersionNumber()).isEqualTo(2);
        assertThat(d.getPreviousVersionId()).isEqualTo(previousVersionId);
        assertThat(d.getTitle()).isEqualTo("title");
        assertThat(d.getContext()).isEqualTo("context");
        assertThat(d.getInstructions()).isEqualTo("instructions");
        assertThat(d.getObjectives()).containsExactly("obj");
        assertThat(d.getDeliverables()).containsExactly("del");
        assertThat(d.getConstraints()).containsExactly("con");
        assertThat(d.getAgentExecutionLogId()).isEqualTo(agentExecutionLogId);
        assertThat(d.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void shouldRejectNullIdWhenRestoring() {
        assertThatThrownBy(() -> AssessmentDraft.restore(null, assessmentId(), 1, null,
                "t", "c", "i", List.of(), List.of(), List.of(), null, Instant.now()))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("id");
    }

    @Test
    void shouldRejectVersionNumberLessThanOneWhenRestoring() {
        assertThatThrownBy(() -> AssessmentDraft.restore(UUID.randomUUID(), assessmentId(), 0, null,
                "t", "c", "i", List.of(), List.of(), List.of(), null, Instant.now()))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("versionNumber");
    }

    @Test
    void shouldRejectVersionOneWithNonNullPreviousVersionWhenRestoring() {
        assertThatThrownBy(() -> AssessmentDraft.restore(UUID.randomUUID(), assessmentId(), 1, UUID.randomUUID(),
                "t", "c", "i", List.of(), List.of(), List.of(), null, Instant.now()))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("version 1");
    }

    @Test
    void shouldRejectVersionGreaterThanOneWithNullPreviousVersionWhenRestoring() {
        assertThatThrownBy(() -> AssessmentDraft.restore(UUID.randomUUID(), assessmentId(), 2, null,
                "t", "c", "i", List.of(), List.of(), List.of(), null, Instant.now()))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("previousVersionId");
    }

    @Test
    void shouldRejectNullCreatedAtWhenRestoring() {
        assertThatThrownBy(() -> AssessmentDraft.restore(UUID.randomUUID(), assessmentId(), 1, null,
                "t", "c", "i", List.of(), List.of(), List.of(), null, null))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("createdAt");
    }
}
