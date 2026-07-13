package cl.gradeops.ai.api.assessment.domain.model;

import cl.gradeops.ai.api.shared.domain.exception.DomainInvariantViolationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class AssessmentTest {

    @Test
    void shouldSetDraftStatusAndTimestampsWhenCreatingNewAssessment() {
        Assessment a = Assessment.create("uid-1");

        assertThat(a.getId()).isNotNull();
        assertThat(a.getTeacherUid()).isEqualTo("uid-1");
        assertThat(a.getStatus()).isEqualTo(AssessmentStatus.DRAFT);
        assertThat(a.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldRejectNullTeacherUidWhenCreatingAssessment() {
        assertThatThrownBy(() -> Assessment.create(null))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("teacherUid");
    }

    @Test
    void shouldRejectBlankTeacherUidWhenCreatingAssessment() {
        assertThatThrownBy(() -> Assessment.create("  "))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("teacherUid");
    }

    @Test
    void shouldRestoreAllFieldsVerbatimWhenRestoringFromPersistence() {
        AssessmentId id = new AssessmentId(UUID.randomUUID());
        Instant createdAt = Instant.now().minusSeconds(60);

        Assessment a = Assessment.restore(id, "uid-2", AssessmentStatus.OPEN, createdAt);

        assertThat(a.getId()).isEqualTo(id);
        assertThat(a.getTeacherUid()).isEqualTo("uid-2");
        assertThat(a.getStatus()).isEqualTo(AssessmentStatus.OPEN);
        assertThat(a.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void shouldRejectNullIdWhenRestoringFromPersistence() {
        assertThatThrownBy(() -> Assessment.restore(null, "uid-1", AssessmentStatus.DRAFT, Instant.now()))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("id");
    }

    @Test
    void shouldRejectNullTeacherUidWhenRestoringFromPersistence() {
        assertThatThrownBy(() -> Assessment.restore(new AssessmentId(UUID.randomUUID()), null, AssessmentStatus.DRAFT, Instant.now()))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("teacherUid");
    }

    @Test
    void shouldRejectBlankTeacherUidWhenRestoringFromPersistence() {
        assertThatThrownBy(() -> Assessment.restore(new AssessmentId(UUID.randomUUID()), " ", AssessmentStatus.DRAFT, Instant.now()))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("teacherUid");
    }

    @Test
    void shouldRejectNullStatusWhenRestoringFromPersistence() {
        assertThatThrownBy(() -> Assessment.restore(new AssessmentId(UUID.randomUUID()), "uid-1", null, Instant.now()))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("status");
    }

    @Test
    void shouldRejectNullCreatedAtWhenRestoringFromPersistence() {
        assertThatThrownBy(() -> Assessment.restore(new AssessmentId(UUID.randomUUID()), "uid-1", AssessmentStatus.DRAFT, null))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("createdAt");
    }
}
