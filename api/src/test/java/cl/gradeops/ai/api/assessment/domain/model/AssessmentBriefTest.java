package cl.gradeops.ai.api.assessment.domain.model;

import cl.gradeops.ai.api.shared.domain.exception.DomainInvariantViolationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class AssessmentBriefTest {

    AssessmentId assessmentId() {
        return new AssessmentId(UUID.randomUUID());
    }

    @Test
    void shouldSetIdAndCreatedAtWhenCreatingNewBrief() {
        AssessmentBrief b = AssessmentBrief.create(assessmentId(), "goal", "topic", "basic", "90min", "Java");

        assertThat(b.getId()).isNotNull();
        assertThat(b.getLearningGoal()).isEqualTo("goal");
        assertThat(b.getTopic()).isEqualTo("topic");
        assertThat(b.getLevel()).isEqualTo("basic");
        assertThat(b.getDuration()).isEqualTo("90min");
        assertThat(b.getLanguage()).isEqualTo("Java");
        assertThat(b.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldRejectNullAssessmentIdWhenCreatingBrief() {
        assertThatThrownBy(() -> AssessmentBrief.create(null, "goal", "topic", "basic", "90min", "Java"))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("assessmentId");
    }

    @Test
    void shouldRejectBlankLearningGoalWhenCreatingBrief() {
        assertThatThrownBy(() -> AssessmentBrief.create(assessmentId(), " ", "topic", "basic", "90min", "Java"))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("learningGoal");
    }

    @Test
    void shouldRejectBlankTopicWhenCreatingBrief() {
        assertThatThrownBy(() -> AssessmentBrief.create(assessmentId(), "goal", "", "basic", "90min", "Java"))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("topic");
    }

    @Test
    void shouldRejectNullLevelWhenCreatingBrief() {
        assertThatThrownBy(() -> AssessmentBrief.create(assessmentId(), "goal", "topic", null, "90min", "Java"))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("level");
    }

    @Test
    void shouldRejectBlankDurationWhenCreatingBrief() {
        assertThatThrownBy(() -> AssessmentBrief.create(assessmentId(), "goal", "topic", "basic", " ", "Java"))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("duration");
    }

    @Test
    void shouldRejectNullLanguageWhenCreatingBrief() {
        assertThatThrownBy(() -> AssessmentBrief.create(assessmentId(), "goal", "topic", "basic", "90min", null))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("language");
    }

    @Test
    void shouldRestoreAllFieldsVerbatimWhenRestoringFromPersistence() {
        UUID id = UUID.randomUUID();
        AssessmentId assessmentId = assessmentId();
        Instant createdAt = Instant.now().minusSeconds(60);

        AssessmentBrief b = AssessmentBrief.restore(id, assessmentId, "goal", "topic", "basic", "90min", "Java", createdAt);

        assertThat(b.getId()).isEqualTo(id);
        assertThat(b.getAssessmentId()).isEqualTo(assessmentId);
        assertThat(b.getLearningGoal()).isEqualTo("goal");
        assertThat(b.getTopic()).isEqualTo("topic");
        assertThat(b.getLevel()).isEqualTo("basic");
        assertThat(b.getDuration()).isEqualTo("90min");
        assertThat(b.getLanguage()).isEqualTo("Java");
        assertThat(b.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void shouldRejectNullIdWhenRestoringFromPersistence() {
        assertThatThrownBy(() -> AssessmentBrief.restore(null, assessmentId(), "goal", "topic", "basic", "90min", "Java", Instant.now()))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("id");
    }

    @Test
    void shouldRejectNullAssessmentIdWhenRestoringFromPersistence() {
        assertThatThrownBy(() -> AssessmentBrief.restore(UUID.randomUUID(), null, "goal", "topic", "basic", "90min", "Java", Instant.now()))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("assessmentId");
    }

    @Test
    void shouldRejectNullCreatedAtWhenRestoringFromPersistence() {
        assertThatThrownBy(() -> AssessmentBrief.restore(UUID.randomUUID(), assessmentId(), "goal", "topic", "basic", "90min", "Java", null))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("createdAt");
    }
}
