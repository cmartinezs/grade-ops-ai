package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

import cl.gradeops.ai.api.assessment.domain.model.AgentExecutionLog;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AgentExecutionLogPersistenceAdapterTest {

    @Mock AgentExecutionLogJpaRepository jpaRepository;
    AgentExecutionLogPersistenceMapper mapper = new AgentExecutionLogPersistenceMapper();

    AgentExecutionLogPersistenceAdapter adapter() {
        return new AgentExecutionLogPersistenceAdapter(jpaRepository, mapper);
    }

    @Test
    void shouldMapDomainToEntityAndDelegateToJpaRepositoryWhenSaving() {
        AssessmentId assessmentId = new AssessmentId(UUID.randomUUID());
        Instant startedAt = Instant.now().minusSeconds(2);
        Instant finishedAt = Instant.now();
        AgentExecutionLog log = AgentExecutionLog.create(assessmentId, UUID.randomUUID(), "assessment", "groq",
                "model-1", "v1", "in-hash", "out-hash", 100, 200, 0.01, "COMPLETED", null, startedAt, finishedAt);

        adapter().save(log);

        ArgumentCaptor<AgentExecutionLogJpaEntity> captor = ArgumentCaptor.forClass(AgentExecutionLogJpaEntity.class);
        verify(jpaRepository).save(captor.capture());
        AgentExecutionLogJpaEntity entity = captor.getValue();
        assertThat(entity.getId()).isEqualTo(log.getId());
        assertThat(entity.getAssessmentId()).isEqualTo(assessmentId.value());
        assertThat(entity.getDraftId()).isNull();
        assertThat(entity.getProvider()).isEqualTo("groq");
        assertThat(entity.getModel()).isEqualTo("model-1");
        assertThat(entity.getStatus()).isEqualTo("COMPLETED");
        assertThat(entity.getErrorCode()).isNull();
        assertThat(entity.getEstimatedInputTokens()).isEqualTo(100);
        assertThat(entity.getCostEstimate()).isEqualTo(0.01);
    }

    @Test
    void shouldSaveWithBackfilledDraftId() {
        AssessmentId assessmentId = new AssessmentId(UUID.randomUUID());
        Instant now = Instant.now();
        AgentExecutionLog log = AgentExecutionLog.create(assessmentId, UUID.randomUUID(), "assessment", "groq",
                "model-1", "v1", "in-hash", "out-hash", 100, 200, 0.01, "COMPLETED", null, now, now);
        UUID draftId = UUID.randomUUID();

        adapter().save(log.withDraftId(draftId));

        ArgumentCaptor<AgentExecutionLogJpaEntity> captor = ArgumentCaptor.forClass(AgentExecutionLogJpaEntity.class);
        verify(jpaRepository).save(captor.capture());
        assertThat(captor.getValue().getDraftId()).isEqualTo(draftId);
    }
}
