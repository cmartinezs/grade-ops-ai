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
    void shouldDefaultToNullCurrentRevisionAndZeroLockVersionWhenCreatingNewAssessment() {
        Assessment a = Assessment.create("uid-1");

        assertThat(a.getCurrentRevisionId()).isNull();
        assertThat(a.getLockVersion()).isZero();
    }

    @Test
    void shouldDefaultToNullCurrentRevisionAndZeroLockVersionWhenRestoringLegacyFourArgShape() {
        Assessment a = Assessment.restore(new AssessmentId(UUID.randomUUID()), "uid-2", AssessmentStatus.OPEN, Instant.now());

        assertThat(a.getCurrentRevisionId()).isNull();
        assertThat(a.getLockVersion()).isZero();
    }

    @Test
    void shouldRestoreCurrentRevisionAndLockVersionVerbatimWhenRestoringFullShape() {
        AssessmentId id = new AssessmentId(UUID.randomUUID());
        Instant createdAt = Instant.now().minusSeconds(60);
        UUID currentRevisionId = UUID.randomUUID();

        Assessment a = Assessment.restore(id, "uid-2", AssessmentStatus.OPEN, createdAt, currentRevisionId, 3);

        assertThat(a.getCurrentRevisionId()).isEqualTo(currentRevisionId);
        assertThat(a.getLockVersion()).isEqualTo(3);
    }

    @Test
    void shouldReturnNewInstanceWithUpdatedCurrentRevisionWhenWithCurrentRevisionCalled() {
        Assessment a = Assessment.create("uid-1");

        Assessment updated = a.withCurrentRevision(UUID.randomUUID());

        assertThat(updated).isNotSameAs(a);
        assertThat(updated.getCurrentRevisionId()).isNotNull();
        assertThat(updated.getLockVersion()).isEqualTo(a.getLockVersion());
        assertThat(a.getCurrentRevisionId()).isNull();
    }

    @Test
    void shouldRejectNullRevisionIdWhenCallingWithCurrentRevision() {
        Assessment a = Assessment.create("uid-1");

        assertThatThrownBy(() -> a.withCurrentRevision(null))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("revisionId");
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
