package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

import cl.gradeops.ai.api.assessment.application.result.AssessmentSummaryResult;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AssessmentPersistenceAdapterTest {

    @Mock AssessmentJpaRepository jpaRepository;
    AssessmentPersistenceMapper mapper = new AssessmentPersistenceMapper();

    AssessmentPersistenceAdapter adapter() {
        return new AssessmentPersistenceAdapter(jpaRepository, mapper);
    }

    Assessment sampleAssessment(UUID id) {
        return Assessment.restore(new AssessmentId(id), "uid-1", AssessmentStatus.DRAFT, Instant.now());
    }

    AssessmentJpaEntity sampleJpaEntity(UUID id) {
        AssessmentJpaEntity e = new AssessmentJpaEntity();
        e.setId(id);
        e.setTeacherUid("uid-1");
        e.setStatus("DRAFT");
        e.setCreatedAt(Instant.now());
        return e;
    }

    @Test
    void shouldMapDomainToEntityAndDelegateToJpaRepositoryWhenSaving() {
        UUID id = UUID.randomUUID();
        adapter().save(sampleAssessment(id));

        ArgumentCaptor<AssessmentJpaEntity> captor = ArgumentCaptor.forClass(AssessmentJpaEntity.class);
        verify(jpaRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(id);
        assertThat(captor.getValue().getTeacherUid()).isEqualTo("uid-1");
        assertThat(captor.getValue().getStatus()).isEqualTo("DRAFT");
    }

    @Test
    void shouldMapEntityToDomainWhenAssessmentFoundById() {
        UUID id = UUID.randomUUID();
        when(jpaRepository.findById(id)).thenReturn(Optional.of(sampleJpaEntity(id)));

        Optional<Assessment> result = adapter().findById(new AssessmentId(id));

        assertThat(result).isPresent();
        assertThat(result.get().getId().value()).isEqualTo(id);
        assertThat(result.get().getTeacherUid()).isEqualTo("uid-1");
        assertThat(result.get().getStatus()).isEqualTo(AssessmentStatus.DRAFT);
    }

    @Test
    void shouldReturnEmptyWhenAssessmentNotFoundById() {
        UUID id = UUID.randomUUID();
        when(jpaRepository.findById(id)).thenReturn(Optional.empty());

        assertThat(adapter().findById(new AssessmentId(id))).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenTeacherHasNoAssessments() {
        when(jpaRepository.findSummariesByTeacherUid("uid-1")).thenReturn(List.of());

        assertThat(adapter().findAllByTeacherId("uid-1")).isEmpty();
    }

    @Test
    void shouldMapProjectionRowsToAssessmentSummaryResults() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        AssessmentSummaryProjection p1 = org.mockito.Mockito.mock(AssessmentSummaryProjection.class);
        when(p1.getId()).thenReturn(id1);
        when(p1.getStatus()).thenReturn("OPEN");
        when(p1.getTitle()).thenReturn("Java Loops Quiz");
        AssessmentSummaryProjection p2 = org.mockito.Mockito.mock(AssessmentSummaryProjection.class);
        when(p2.getId()).thenReturn(id2);
        when(p2.getStatus()).thenReturn("DRAFT");
        when(p2.getTitle()).thenReturn(null);
        when(jpaRepository.findSummariesByTeacherUid("uid-1")).thenReturn(List.of(p1, p2));

        List<AssessmentSummaryResult> result = adapter().findAllByTeacherId("uid-1");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(id1.toString());
        assertThat(result.get(0).title()).isEqualTo("Java Loops Quiz");
        assertThat(result.get(0).status()).isEqualTo(AssessmentStatus.OPEN);
        assertThat(result.get(0).submissionCount()).isZero();
        assertThat(result.get(0).pendingApprovals()).isZero();
        assertThat(result.get(0).reportLink()).isNull();
        assertThat(result.get(1).id()).isEqualTo(id2.toString());
        assertThat(result.get(1).title()).isNull();
        assertThat(result.get(1).status()).isEqualTo(AssessmentStatus.DRAFT);
    }

    @Test
    void shouldRoundTripAssessmentThroughSaveAndFindById() {
        UUID id = UUID.randomUUID();
        AssessmentJpaEntity captured = sampleJpaEntity(id);
        Assessment original = mapper.toDomain(captured);

        when(jpaRepository.findById(id)).thenReturn(Optional.of(captured));

        Optional<Assessment> result = adapter().findById(new AssessmentId(id));

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(original.getId());
        assertThat(result.get().getTeacherUid()).isEqualTo(original.getTeacherUid());
        assertThat(result.get().getStatus()).isEqualTo(original.getStatus());
        assertThat(result.get().getCreatedAt()).isEqualTo(original.getCreatedAt());
    }
}
