package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

import cl.gradeops.ai.api.assessment.domain.model.AssessmentBrief;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssessmentBriefPersistenceAdapterTest {

    @Mock AssessmentBriefJpaRepository jpaRepository;
    AssessmentBriefPersistenceMapper mapper = new AssessmentBriefPersistenceMapper();

    AssessmentBriefPersistenceAdapter adapter() {
        return new AssessmentBriefPersistenceAdapter(jpaRepository, mapper);
    }

    AssessmentBrief sampleBrief(UUID id, UUID assessmentId) {
        return AssessmentBrief.restore(id, new AssessmentId(assessmentId), "goal", "topic", "basic", "90min", "Java", Instant.now());
    }

    AssessmentBriefJpaEntity sampleJpaEntity(UUID id, UUID assessmentId) {
        AssessmentBriefJpaEntity e = new AssessmentBriefJpaEntity();
        e.setId(id);
        e.setAssessmentId(assessmentId);
        e.setLearningGoal("goal");
        e.setTopic("topic");
        e.setLevel("basic");
        e.setDuration("90min");
        e.setLanguage("Java");
        e.setCreatedAt(Instant.now());
        return e;
    }

    @Test
    void shouldMapDomainToEntityAndDelegateToJpaRepositoryWhenSaving() {
        UUID id = UUID.randomUUID();
        UUID assessmentId = UUID.randomUUID();
        adapter().save(sampleBrief(id, assessmentId));

        ArgumentCaptor<AssessmentBriefJpaEntity> captor = ArgumentCaptor.forClass(AssessmentBriefJpaEntity.class);
        verify(jpaRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(id);
        assertThat(captor.getValue().getAssessmentId()).isEqualTo(assessmentId);
        assertThat(captor.getValue().getLearningGoal()).isEqualTo("goal");
        assertThat(captor.getValue().getTopic()).isEqualTo("topic");
        assertThat(captor.getValue().getLevel()).isEqualTo("basic");
        assertThat(captor.getValue().getDuration()).isEqualTo("90min");
        assertThat(captor.getValue().getLanguage()).isEqualTo("Java");
    }

    @Test
    void shouldMapEntityToDomainWhenBriefFoundByAssessmentId() {
        UUID id = UUID.randomUUID();
        UUID assessmentId = UUID.randomUUID();
        when(jpaRepository.findByAssessmentId(assessmentId)).thenReturn(Optional.of(sampleJpaEntity(id, assessmentId)));

        Optional<AssessmentBrief> result = adapter().findByAssessmentId(new AssessmentId(assessmentId));

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(id);
        assertThat(result.get().getAssessmentId().value()).isEqualTo(assessmentId);
        assertThat(result.get().getLearningGoal()).isEqualTo("goal");
    }

    @Test
    void shouldReturnEmptyWhenBriefNotFoundByAssessmentId() {
        UUID assessmentId = UUID.randomUUID();
        when(jpaRepository.findByAssessmentId(assessmentId)).thenReturn(Optional.empty());

        assertThat(adapter().findByAssessmentId(new AssessmentId(assessmentId))).isEmpty();
    }

    @Test
    void shouldRoundTripBriefThroughSaveAndFindByAssessmentId() {
        UUID id = UUID.randomUUID();
        UUID assessmentId = UUID.randomUUID();
        AssessmentBriefJpaEntity captured = sampleJpaEntity(id, assessmentId);
        AssessmentBrief original = mapper.toDomain(captured);

        when(jpaRepository.findByAssessmentId(assessmentId)).thenReturn(Optional.of(captured));

        Optional<AssessmentBrief> result = adapter().findByAssessmentId(new AssessmentId(assessmentId));

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(original.getId());
        assertThat(result.get().getAssessmentId()).isEqualTo(original.getAssessmentId());
        assertThat(result.get().getLearningGoal()).isEqualTo(original.getLearningGoal());
        assertThat(result.get().getTopic()).isEqualTo(original.getTopic());
        assertThat(result.get().getLevel()).isEqualTo(original.getLevel());
        assertThat(result.get().getDuration()).isEqualTo(original.getDuration());
        assertThat(result.get().getLanguage()).isEqualTo(original.getLanguage());
        assertThat(result.get().getCreatedAt()).isEqualTo(original.getCreatedAt());
    }
}
