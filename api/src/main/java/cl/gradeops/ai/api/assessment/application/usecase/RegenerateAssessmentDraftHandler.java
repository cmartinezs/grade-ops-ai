package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.agentclient.AssessmentCommand;
import cl.gradeops.ai.api.assessment.application.command.RegenerateAssessmentDraftCommand;
import cl.gradeops.ai.api.assessment.application.exception.StaleRevisionException;
import cl.gradeops.ai.api.assessment.application.port.in.RegenerateAssessmentDraftUseCase;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentBriefRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRevisionRepositoryPort;
import cl.gradeops.ai.api.assessment.application.result.GenerateAssessmentDraftResult;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentBrief;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentRevision;
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyGuard;
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyPayloadHasher;
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyRecord;
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyScope;
import cl.gradeops.ai.api.shared.application.security.OwnershipVerifier;
import cl.gradeops.ai.api.shared.domain.exception.ResourceNotFoundException;

import java.util.Optional;
import java.util.UUID;

/**
 * NO @Service — declared as @Bean in AssessmentConfig (task-05 pattern).
 *
 * <p>Session A3 (Task 09) migrates this handler off the legacy {@code AssessmentDraft}/{@code
 * AgentExecutionLog} model onto {@code AssessmentRevision}, routing through the same durable
 * {@link AiOperationCoordinator} Session A2 built for initial generation. An assessment produced
 * by {@code GenerateAssessmentDraftHandler} (new, revision-based flow) can regenerate through
 * this handler exactly as one produced before this cut, since both leave {@code
 * Assessment.currentRevisionId} pointing at a real {@code AssessmentRevision} — there is no
 * separate legacy path here anymore.
 *
 * <p>The pre-dispatch {@code expectedRevisionId} check happens in this method, before the
 * coordinator is ever called — a stale request never reaches Phase 0, never touches an {@code
 * AiOperation}, and never calls {@code agents/}.
 */
public class RegenerateAssessmentDraftHandler implements RegenerateAssessmentDraftUseCase {

    private static final String OPERATION_TYPE = "REGENERATE_REVISION";

    private final AssessmentRepositoryPort assessmentRepository;
    private final AssessmentBriefRepositoryPort assessmentBriefRepository;
    private final AssessmentRevisionRepositoryPort assessmentRevisionRepository;
    private final OwnershipVerifier ownershipVerifier;
    private final IdempotencyGuard idempotencyGuard;
    private final AiOperationCoordinator aiOperationCoordinator;

    public RegenerateAssessmentDraftHandler(AssessmentRepositoryPort assessmentRepository,
                                             AssessmentBriefRepositoryPort assessmentBriefRepository,
                                             AssessmentRevisionRepositoryPort assessmentRevisionRepository,
                                             OwnershipVerifier ownershipVerifier,
                                             IdempotencyGuard idempotencyGuard,
                                             AiOperationCoordinator aiOperationCoordinator) {
        this.assessmentRepository = assessmentRepository;
        this.assessmentBriefRepository = assessmentBriefRepository;
        this.assessmentRevisionRepository = assessmentRevisionRepository;
        this.ownershipVerifier = ownershipVerifier;
        this.idempotencyGuard = idempotencyGuard;
        this.aiOperationCoordinator = aiOperationCoordinator;
    }

    @Override
    public GenerateAssessmentDraftResult execute(RegenerateAssessmentDraftCommand command) {
        AssessmentId assessmentId = new AssessmentId(command.assessmentId());
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException(assessmentId.value().toString()));
        ownershipVerifier.verify(assessment.getTeacherUid(), command.teacherUid(), assessmentId.value().toString());

        IdempotencyScope scope = IdempotencyScope.assessment(assessmentId.value());
        String payloadHash = IdempotencyPayloadHasher.hash(
                assessmentId.value().toString(), command.expectedRevisionId().toString(), command.adjustmentNotes());
        Optional<IdempotencyRecord> prior =
                idempotencyGuard.check(scope, OPERATION_TYPE, command.idempotencyKey(), payloadHash);
        if (prior.isPresent()) {
            return replay(prior.get());
        }

        // Pre-dispatch staleness check — must reject before Phase 0/the agent call, not only at
        // persist time (Idempotency and Concurrency Strategy ADR). Zero AiOperation/AgentAttempt
        // rows are touched and assessmentAgentClient.generate(...) is never invoked on this path.
        if (assessment.getCurrentRevisionId() == null
                || !assessment.getCurrentRevisionId().equals(command.expectedRevisionId())) {
            throw new StaleRevisionException(assessmentId.value().toString());
        }

        AssessmentBrief brief = assessmentBriefRepository.findByAssessmentId(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException(assessmentId.value().toString()));
        AssessmentRevision expectedRevision = assessmentRevisionRepository.findById(command.expectedRevisionId())
                .orElseThrow(() -> new ResourceNotFoundException(command.expectedRevisionId().toString()));

        AssessmentCommand agentCommand = new AssessmentCommand(
                brief.getLearningGoal(), brief.getTopic(), brief.getLevel(), brief.getDuration(), brief.getLanguage(),
                command.adjustmentNotes(), expectedRevision.getId().toString(), toPromptSummary(expectedRevision),
                null, null);

        AssessmentRevision revision = aiOperationCoordinator.regenerateRevision(assessment, expectedRevision,
                agentCommand, command.teacherUid(), command.adjustmentNotes(), command.idempotencyKey());

        idempotencyGuard.record(scope, OPERATION_TYPE, command.idempotencyKey(), payloadHash,
                revision.getId().toString(), 201);

        return GenerateAssessmentDraftResult.fromRevision(revision);
    }

    private GenerateAssessmentDraftResult replay(IdempotencyRecord record) {
        UUID revisionId = UUID.fromString(record.getResultReference());
        AssessmentRevision revision = assessmentRevisionRepository.findById(revisionId)
                .orElseThrow(() -> new ResourceNotFoundException(revisionId.toString()));
        return GenerateAssessmentDraftResult.fromRevision(revision);
    }

    /** Renders the prior revision's content to text for {@code agents/}'s {@code previousDraft} field. */
    private static String toPromptSummary(AssessmentRevision revision) {
        return "Title: " + revision.getTitle()
                + "\nContext: " + revision.getContext()
                + "\nInstructions: " + revision.getInstructions()
                + "\nObjectives: " + String.join("; ", revision.getObjectives())
                + "\nDeliverables: " + String.join("; ", revision.getDeliverables())
                + "\nConstraints: " + String.join("; ", revision.getConstraints());
    }
}
