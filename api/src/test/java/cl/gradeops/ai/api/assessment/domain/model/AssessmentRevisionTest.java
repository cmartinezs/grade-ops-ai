package cl.gradeops.ai.api.assessment.domain.model;

import cl.gradeops.ai.api.shared.domain.exception.DomainInvariantViolationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class AssessmentRevisionTest {

    AssessmentId assessmentId() {
        return new AssessmentId(UUID.randomUUID());
    }

    AssessmentRevision firstVersion() {
        return AssessmentRevision.generateFromAi(assessmentId(), "title", "context", "instructions",
                List.of("obj1"), List.of("del1"), List.of("con1"), "teacher-1", UUID.randomUUID());
    }

    @Test
    void shouldSetVersionOneAndNullPreviousRevisionWhenGeneratingFromAi() {
        UUID sourceAttemptId = UUID.randomUUID();
        AssessmentRevision r = AssessmentRevision.generateFromAi(assessmentId(), "title", "context", "instructions",
                List.of("obj1"), List.of("del1"), List.of("con1"), "teacher-1", sourceAttemptId);

        assertThat(r.getId()).isNotNull();
        assertThat(r.getVersionNumber()).isEqualTo(1);
        assertThat(r.getPreviousRevisionId()).isNull();
        assertThat(r.getOrigin()).isEqualTo(RevisionOrigin.AI_GENERATED);
        assertThat(r.getActorId()).isEqualTo("teacher-1");
        assertThat(r.getReason()).isNull();
        assertThat(r.getSourceAgentAttemptId()).isEqualTo(sourceAttemptId);
        assertThat(r.getTitle()).isEqualTo("title");
        assertThat(r.getContext()).isEqualTo("context");
        assertThat(r.getInstructions()).isEqualTo("instructions");
        assertThat(r.getObjectives()).containsExactly("obj1");
        assertThat(r.getDeliverables()).containsExactly("del1");
        assertThat(r.getConstraints()).containsExactly("con1");
        assertThat(r.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldRejectNullSourceAgentAttemptIdWhenGeneratingFromAi() {
        assertThatThrownBy(() -> AssessmentRevision.generateFromAi(assessmentId(), "title", "context", "instructions",
                List.of(), List.of(), List.of(), "teacher-1", null))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("sourceAgentAttemptId");
    }

    @Test
    void shouldRejectBlankActorIdWhenGeneratingFromAi() {
        assertThatThrownBy(() -> AssessmentRevision.generateFromAi(assessmentId(), "title", "context", "instructions",
                List.of(), List.of(), List.of(), " ", UUID.randomUUID()))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("actorId");
    }

    @Test
    void shouldRejectNullAssessmentIdWhenGeneratingFromAi() {
        assertThatThrownBy(() -> AssessmentRevision.generateFromAi(null, "title", "context", "instructions",
                List.of(), List.of(), List.of(), "teacher-1", UUID.randomUUID()))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("assessmentId");
    }

    @Test
    void shouldRejectBlankTitleWhenGeneratingFromAi() {
        assertThatThrownBy(() -> AssessmentRevision.generateFromAi(assessmentId(), " ", "context", "instructions",
                List.of(), List.of(), List.of(), "teacher-1", UUID.randomUUID()))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("title");
    }

    @Test
    void shouldReturnImmutableListsFromGetters() {
        AssessmentRevision r = firstVersion();

        assertThatThrownBy(() -> r.getObjectives().add("x")).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> r.getDeliverables().add("x")).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> r.getConstraints().add("x")).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldNotBeAffectedByMutatingTheOriginalListAfterGeneratingFromAi() {
        List<String> objectives = new ArrayList<>(List.of("obj1"));
        AssessmentRevision r = AssessmentRevision.generateFromAi(assessmentId(), "title", "context", "instructions",
                objectives, List.of("del1"), List.of("con1"), "teacher-1", UUID.randomUUID());

        objectives.add("obj2");

        assertThat(r.getObjectives()).containsExactly("obj1");
    }

    @Test
    void shouldIncrementVersionAndChainToPreviousWhenRegeneratingFromAi() {
        AssessmentRevision v1 = firstVersion();
        UUID newAttemptId = UUID.randomUUID();

        AssessmentRevision v2 = AssessmentRevision.regenerateFromAi(v1, "title2", "context2", "instructions2",
                List.of("obj2"), List.of("del2"), List.of("con2"), "teacher-1", "adjust it", newAttemptId);

        assertThat(v2.getId()).isNotEqualTo(v1.getId());
        assertThat(v2.getVersionNumber()).isEqualTo(2);
        assertThat(v2.getPreviousRevisionId()).isEqualTo(v1.getId());
        assertThat(v2.getAssessmentId()).isEqualTo(v1.getAssessmentId());
        assertThat(v2.getOrigin()).isEqualTo(RevisionOrigin.AI_GENERATED);
        assertThat(v2.getReason()).isEqualTo("adjust it");
        assertThat(v2.getSourceAgentAttemptId()).isEqualTo(newAttemptId);
        assertThat(v2.getTitle()).isEqualTo("title2");
    }

    @Test
    void shouldChainVersionNumbersAcrossMultipleRegenerations() {
        AssessmentRevision v1 = firstVersion();
        AssessmentRevision v2 = AssessmentRevision.regenerateFromAi(v1, "t2", "c2", "i2",
                List.of(), List.of(), List.of(), "teacher-1", null, UUID.randomUUID());
        AssessmentRevision v3 = AssessmentRevision.regenerateFromAi(v2, "t3", "c3", "i3",
                List.of(), List.of(), List.of(), "teacher-1", null, UUID.randomUUID());

        assertThat(v3.getVersionNumber()).isEqualTo(3);
        assertThat(v3.getPreviousRevisionId()).isEqualTo(v2.getId());
    }

    @Test
    void shouldRejectNullPreviousRevisionWhenRegeneratingFromAi() {
        assertThatThrownBy(() -> AssessmentRevision.regenerateFromAi(null, "t", "c", "i",
                List.of(), List.of(), List.of(), "teacher-1", null, UUID.randomUUID()))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("previousRevision");
    }

    @Test
    void shouldRejectNullSourceAgentAttemptIdWhenRegeneratingFromAi() {
        AssessmentRevision v1 = firstVersion();

        assertThatThrownBy(() -> AssessmentRevision.regenerateFromAi(v1, "t2", "c2", "i2",
                List.of(), List.of(), List.of(), "teacher-1", null, null))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("sourceAgentAttemptId");
    }

    @Test
    void shouldSetHumanEditedOriginAndNullSourceAttemptWhenCreatingFromHumanEdit() {
        AssessmentRevision v1 = firstVersion();

        AssessmentRevision v2 = AssessmentRevision.createFromHumanEdit(v1, "edited title", "context2", "instructions2",
                List.of("obj2"), List.of("del2"), List.of("con2"), "teacher-2", "fixed a typo");

        assertThat(v2.getId()).isNotEqualTo(v1.getId());
        assertThat(v2.getVersionNumber()).isEqualTo(2);
        assertThat(v2.getPreviousRevisionId()).isEqualTo(v1.getId());
        assertThat(v2.getAssessmentId()).isEqualTo(v1.getAssessmentId());
        assertThat(v2.getOrigin()).isEqualTo(RevisionOrigin.HUMAN_EDITED);
        assertThat(v2.getActorId()).isEqualTo("teacher-2");
        assertThat(v2.getReason()).isEqualTo("fixed a typo");
        assertThat(v2.getSourceAgentAttemptId()).isNull();
        assertThat(v2.getTitle()).isEqualTo("edited title");
    }

    @Test
    void shouldNeverOverwriteThePreviousAiGeneratedRevisionWhenCreatingFromHumanEdit() {
        AssessmentRevision v1 = firstVersion();

        AssessmentRevision v2 = AssessmentRevision.createFromHumanEdit(v1, "edited title", "context2", "instructions2",
                List.of("obj2"), List.of("del2"), List.of("con2"), "teacher-2", null);

        assertThat(v1.getTitle()).isEqualTo("title");
        assertThat(v1.getOrigin()).isEqualTo(RevisionOrigin.AI_GENERATED);
        assertThat(v2.getTitle()).isEqualTo("edited title");
    }

    @Test
    void shouldRejectNullPreviousRevisionWhenCreatingFromHumanEdit() {
        assertThatThrownBy(() -> AssessmentRevision.createFromHumanEdit(null, "t", "c", "i",
                List.of(), List.of(), List.of(), "teacher-1", null))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("previousRevision");
    }

    @Test
    void shouldRejectBlankActorIdWhenCreatingFromHumanEdit() {
        AssessmentRevision v1 = firstVersion();

        assertThatThrownBy(() -> AssessmentRevision.createFromHumanEdit(v1, "t", "c", "i",
                List.of(), List.of(), List.of(), " ", null))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("actorId");
    }

    @Test
    void shouldRestoreAllFieldsVerbatimWhenRestoringFromPersistence() {
        UUID id = UUID.randomUUID();
        AssessmentId assessmentId = assessmentId();
        UUID previousRevisionId = UUID.randomUUID();
        UUID sourceAgentAttemptId = UUID.randomUUID();
        Instant createdAt = Instant.now().minusSeconds(60);

        AssessmentRevision r = AssessmentRevision.restore(id, assessmentId, 2, previousRevisionId,
                RevisionOrigin.AI_GENERATED, "teacher-1", "a reason", sourceAgentAttemptId,
                "title", "context", "instructions",
                List.of("obj"), List.of("del"), List.of("con"), createdAt);

        assertThat(r.getId()).isEqualTo(id);
        assertThat(r.getAssessmentId()).isEqualTo(assessmentId);
        assertThat(r.getVersionNumber()).isEqualTo(2);
        assertThat(r.getPreviousRevisionId()).isEqualTo(previousRevisionId);
        assertThat(r.getOrigin()).isEqualTo(RevisionOrigin.AI_GENERATED);
        assertThat(r.getActorId()).isEqualTo("teacher-1");
        assertThat(r.getReason()).isEqualTo("a reason");
        assertThat(r.getSourceAgentAttemptId()).isEqualTo(sourceAgentAttemptId);
        assertThat(r.getTitle()).isEqualTo("title");
        assertThat(r.getContext()).isEqualTo("context");
        assertThat(r.getInstructions()).isEqualTo("instructions");
        assertThat(r.getObjectives()).containsExactly("obj");
        assertThat(r.getDeliverables()).containsExactly("del");
        assertThat(r.getConstraints()).containsExactly("con");
        assertThat(r.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void shouldAllowLegacyUnknownOriginWithNullActorIdOnlyThroughRestore() {
        AssessmentRevision r = AssessmentRevision.restore(UUID.randomUUID(), assessmentId(), 1, null,
                RevisionOrigin.LEGACY_UNKNOWN, null, null, null,
                "t", "c", "i", List.of(), List.of(), List.of(), Instant.now());

        assertThat(r.getOrigin()).isEqualTo(RevisionOrigin.LEGACY_UNKNOWN);
        assertThat(r.getActorId()).isNull();
    }

    @Test
    void shouldRejectNonLegacyOriginWithNullActorIdWhenRestoring() {
        assertThatThrownBy(() -> AssessmentRevision.restore(UUID.randomUUID(), assessmentId(), 1, null,
                RevisionOrigin.AI_GENERATED, null, null, UUID.randomUUID(),
                "t", "c", "i", List.of(), List.of(), List.of(), Instant.now()))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("actorId");
    }

    @Test
    void shouldRejectNonNullActorIdWhenRestoringLegacyUnknown() {
        assertThatThrownBy(() -> AssessmentRevision.restore(UUID.randomUUID(), assessmentId(), 1, null,
                RevisionOrigin.LEGACY_UNKNOWN, "teacher-1", null, null,
                "t", "c", "i", List.of(), List.of(), List.of(), Instant.now()))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("actorId");
    }

    @Test
    void shouldRejectNullIdWhenRestoring() {
        assertThatThrownBy(() -> AssessmentRevision.restore(null, assessmentId(), 1, null,
                RevisionOrigin.AI_GENERATED, "teacher-1", null, UUID.randomUUID(),
                "t", "c", "i", List.of(), List.of(), List.of(), Instant.now()))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("id");
    }

    @Test
    void shouldRejectVersionNumberLessThanOneWhenRestoring() {
        assertThatThrownBy(() -> AssessmentRevision.restore(UUID.randomUUID(), assessmentId(), 0, null,
                RevisionOrigin.AI_GENERATED, "teacher-1", null, UUID.randomUUID(),
                "t", "c", "i", List.of(), List.of(), List.of(), Instant.now()))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("versionNumber");
    }

    @Test
    void shouldRejectVersionOneWithNonNullPreviousRevisionWhenRestoring() {
        assertThatThrownBy(() -> AssessmentRevision.restore(UUID.randomUUID(), assessmentId(), 1, UUID.randomUUID(),
                RevisionOrigin.AI_GENERATED, "teacher-1", null, UUID.randomUUID(),
                "t", "c", "i", List.of(), List.of(), List.of(), Instant.now()))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("version 1");
    }

    @Test
    void shouldRejectVersionGreaterThanOneWithNullPreviousRevisionWhenRestoring() {
        assertThatThrownBy(() -> AssessmentRevision.restore(UUID.randomUUID(), assessmentId(), 2, null,
                RevisionOrigin.AI_GENERATED, "teacher-1", null, UUID.randomUUID(),
                "t", "c", "i", List.of(), List.of(), List.of(), Instant.now()))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("previousRevisionId");
    }

    @Test
    void shouldRejectNullCreatedAtWhenRestoring() {
        assertThatThrownBy(() -> AssessmentRevision.restore(UUID.randomUUID(), assessmentId(), 1, null,
                RevisionOrigin.AI_GENERATED, "teacher-1", null, UUID.randomUUID(),
                "t", "c", "i", List.of(), List.of(), List.of(), null))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("createdAt");
    }

    @Test
    void shouldHaveNoApplyEditMethod() {
        assertThatThrownBy(() -> AssessmentRevision.class.getMethod("applyEdit",
                String.class, String.class, String.class, List.class, List.class, List.class))
                .isInstanceOf(NoSuchMethodException.class);
    }
}
