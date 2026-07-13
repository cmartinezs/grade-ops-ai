package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

import cl.gradeops.ai.api.assessment.domain.model.AssessmentDraft;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssessmentDraftPersistenceAdapterTest {

    @Mock AssessmentDraftJpaRepository jpaRepository;
    AssessmentDraftPersistenceMapper mapper = new AssessmentDraftPersistenceMapper();

    AssessmentDraftPersistenceAdapter adapter() {
        return new AssessmentDraftPersistenceAdapter(jpaRepository, mapper);
    }

    AssessmentDraftJpaEntity sampleJpaEntity(UUID id, UUID assessmentId, int versionNumber, UUID previousVersionId) {
        AssessmentDraftJpaEntity e = new AssessmentDraftJpaEntity();
        e.setId(id);
        e.setAssessmentId(assessmentId);
        e.setVersionNumber(versionNumber);
        e.setPreviousVersionId(previousVersionId);
        e.setTitle("title");
        e.setContext("context");
        e.setInstructions("instructions");
        e.setObjectives(List.of("obj"));
        e.setDeliverables(List.of("del"));
        e.setConstraints(List.of("con"));
        e.setCreatedAt(Instant.now());
        return e;
    }

    @Test
    void shouldMapDomainToEntityAndDelegateToJpaRepositoryWhenSaving() {
        UUID assessmentId = UUID.randomUUID();
        AssessmentDraft draft = AssessmentDraft.generate(new AssessmentId(assessmentId), "title", "context",
                "instructions", List.of("obj"), List.of("del"), List.of("con"), null);

        adapter().save(draft);

        ArgumentCaptor<AssessmentDraftJpaEntity> captor = ArgumentCaptor.forClass(AssessmentDraftJpaEntity.class);
        verify(jpaRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(draft.getId());
        assertThat(captor.getValue().getAssessmentId()).isEqualTo(assessmentId);
        assertThat(captor.getValue().getVersionNumber()).isEqualTo(1);
        assertThat(captor.getValue().getPreviousVersionId()).isNull();
        assertThat(captor.getValue().getObjectives()).containsExactly("obj");
        assertThat(captor.getValue().getDeliverables()).containsExactly("del");
        assertThat(captor.getValue().getConstraints()).containsExactly("con");
    }

    @Test
    void shouldReturnHighestVersionWhenFindingCurrentByAssessmentId() {
        UUID assessmentId = UUID.randomUUID();
        UUID v1Id = UUID.randomUUID();
        UUID v2Id = UUID.randomUUID();
        UUID v3Id = UUID.randomUUID();
        // repository contract returns descending by version_number — the adapter must not re-sort
        when(jpaRepository.findAllByAssessmentIdOrderByVersionNumberDesc(assessmentId)).thenReturn(List.of(
                sampleJpaEntity(v3Id, assessmentId, 3, v2Id),
                sampleJpaEntity(v2Id, assessmentId, 2, v1Id),
                sampleJpaEntity(v1Id, assessmentId, 1, null)
        ));

        Optional<AssessmentDraft> current = adapter().findCurrentByAssessmentId(new AssessmentId(assessmentId));

        assertThat(current).isPresent();
        assertThat(current.get().getId()).isEqualTo(v3Id);
        assertThat(current.get().getVersionNumber()).isEqualTo(3);
    }

    @Test
    void shouldReturnEmptyWhenNoDraftExistsForAssessment() {
        UUID assessmentId = UUID.randomUUID();
        when(jpaRepository.findAllByAssessmentIdOrderByVersionNumberDesc(assessmentId)).thenReturn(List.of());

        assertThat(adapter().findCurrentByAssessmentId(new AssessmentId(assessmentId))).isEmpty();
    }

    @Test
    void shouldReturnAllVersionsWhenFindingAllByAssessmentId() {
        UUID assessmentId = UUID.randomUUID();
        UUID v1Id = UUID.randomUUID();
        UUID v2Id = UUID.randomUUID();
        when(jpaRepository.findAllByAssessmentIdOrderByVersionNumberDesc(assessmentId)).thenReturn(List.of(
                sampleJpaEntity(v2Id, assessmentId, 2, v1Id),
                sampleJpaEntity(v1Id, assessmentId, 1, null)
        ));

        List<AssessmentDraft> all = adapter().findAllByAssessmentId(new AssessmentId(assessmentId));

        assertThat(all).hasSize(2);
        assertThat(all.get(0).getVersionNumber()).isEqualTo(2);
        assertThat(all.get(1).getVersionNumber()).isEqualTo(1);
    }
}
