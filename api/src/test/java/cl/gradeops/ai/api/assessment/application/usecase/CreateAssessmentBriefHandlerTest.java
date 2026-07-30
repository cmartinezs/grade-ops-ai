package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.assessment.application.command.CreateAssessmentBriefCommand;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentBriefRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRepositoryPort;
import cl.gradeops.ai.api.assessment.application.result.CreateAssessmentBriefResult;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentBrief;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentStatus;
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyGuard;
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyRecord;
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyScope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Covers this handler's own idempotency-guard responsibilities (A3 Contract Correction §
 * Correction 1) with the transaction manager and repositories mocked — the atomic
 * Assessment/AssessmentBrief/IdempotencyRecord write itself, and the concurrent-duplicate replay
 * path, are exercised for real against Postgres by {@link CreateAssessmentBriefHandlerIntegrationTest}.
 */
@ExtendWith(MockitoExtension.class)
class CreateAssessmentBriefHandlerTest {

    @Mock AssessmentRepositoryPort assessmentRepository;
    @Mock AssessmentBriefRepositoryPort assessmentBriefRepository;
    @Mock IdempotencyGuard idempotencyGuard;
    @Mock PlatformTransactionManager transactionManager;
    @Mock TransactionStatus transactionStatus;

    CreateAssessmentBriefHandler handler() {
        return new CreateAssessmentBriefHandler(assessmentRepository, assessmentBriefRepository,
                idempotencyGuard, transactionManager);
    }

    @BeforeEach
    void setUp() {
        lenient().when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);
    }

    @Test
    void shouldCreateAssessmentInDraftStatusAndItsBriefLinkedByIdThenRecordIdempotency() {
        when(idempotencyGuard.check(any(), eq("CREATE_ASSESSMENT_BRIEF"), eq("key-1"), any()))
                .thenReturn(Optional.empty());
        CreateAssessmentBriefCommand command = new CreateAssessmentBriefCommand(
                "uid-1", "Evaluate loops", "Java loops", "basic", "90min", "Java", "key-1");

        CreateAssessmentBriefResult result = handler().execute(command);

        ArgumentCaptor<Assessment> assessmentCaptor = ArgumentCaptor.forClass(Assessment.class);
        verify(assessmentRepository).save(assessmentCaptor.capture());
        Assessment savedAssessment = assessmentCaptor.getValue();
        assertThat(savedAssessment.getTeacherUid()).isEqualTo("uid-1");
        assertThat(savedAssessment.getStatus()).isEqualTo(AssessmentStatus.DRAFT);

        ArgumentCaptor<AssessmentBrief> briefCaptor = ArgumentCaptor.forClass(AssessmentBrief.class);
        verify(assessmentBriefRepository).save(briefCaptor.capture());
        AssessmentBrief savedBrief = briefCaptor.getValue();
        assertThat(savedBrief.getAssessmentId()).isEqualTo(savedAssessment.getId());
        assertThat(savedBrief.getLearningGoal()).isEqualTo("Evaluate loops");
        assertThat(savedBrief.getTopic()).isEqualTo("Java loops");
        assertThat(savedBrief.getLevel()).isEqualTo("basic");
        assertThat(savedBrief.getDuration()).isEqualTo("90min");
        assertThat(savedBrief.getLanguage()).isEqualTo("Java");

        assertThat(result.assessmentId()).isEqualTo(savedAssessment.getId().value().toString());

        ArgumentCaptor<IdempotencyScope> scopeCaptor = ArgumentCaptor.forClass(IdempotencyScope.class);
        verify(idempotencyGuard).record(scopeCaptor.capture(), eq("CREATE_ASSESSMENT_BRIEF"), eq("key-1"),
                any(), eq(result.assessmentId()), eq(201));
        assertThat(scopeCaptor.getValue()).isEqualTo(IdempotencyScope.teacher("uid-1"));
    }

    @Test
    void shouldReplayPriorRecordWithoutCreatingAnAssessmentWhenIdempotencyKeyAlreadyUsed() {
        IdempotencyRecord priorRecord = IdempotencyRecord.create(IdempotencyScope.teacher("uid-1"),
                "CREATE_ASSESSMENT_BRIEF", "key-1", "hash", "assess-existing-1", 201);
        when(idempotencyGuard.check(any(), eq("CREATE_ASSESSMENT_BRIEF"), eq("key-1"), any()))
                .thenReturn(Optional.of(priorRecord));

        CreateAssessmentBriefResult result = handler().execute(new CreateAssessmentBriefCommand(
                "uid-1", "Evaluate loops", "Java loops", "basic", "90min", "Java", "key-1"));

        assertThat(result.assessmentId()).isEqualTo("assess-existing-1");
        verifyNoInteractions(assessmentRepository, assessmentBriefRepository, transactionManager);
    }
}
