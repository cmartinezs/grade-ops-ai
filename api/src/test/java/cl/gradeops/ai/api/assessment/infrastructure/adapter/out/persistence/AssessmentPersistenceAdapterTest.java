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
        when(jpaRepository.findAllByTeacherUid("uid-1")).thenReturn(List.of());

        List<AssessmentSummaryResult> result = adapter().findAllByTeacherId("uid-1");

        assertThat(result).isEmpty();
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
