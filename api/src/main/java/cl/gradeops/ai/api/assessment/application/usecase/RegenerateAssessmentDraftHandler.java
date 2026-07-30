package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.agentclient.AssessmentCommand;
import cl.gradeops.ai.api.assessment.application.command.RegenerateAssessmentDraftCommand;
import cl.gradeops.ai.api.assessment.application.exception.OperationInProgressException;
import cl.gradeops.ai.api.assessment.application.exception.StaleRevisionException;
import cl.gradeops.ai.api.assessment.application.port.in.RegenerateAssessmentDraftUseCase;
import cl.gradeops.ai.api.assessment.application.port.out.AgentAttemptRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AiOperationRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentBriefRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRevisionRepositoryPort;
import cl.gradeops.ai.api.assessment.application.result.GenerateAssessmentDraftResult;
import cl.gradeops.ai.api.assessment.domain.model.AgentAttempt;
import cl.gradeops.ai.api.assessment.domain.model.AiOperation;
import cl.gradeops.ai.api.assessment.domain.model.AiOperationStatus;
import cl.gradeops.ai.api.assessment.domain.model.AiOperationType;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentBrief;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentRevision;
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyCompletionContext;
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyGuard;
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyPayloadHasher;
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyRecord;
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyScope;
import cl.gradeops.ai.api.shared.application.security.OwnershipVerifier;
import cl.gradeops.ai.api.shared.domain.exception.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
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
 *
 * <p>A3 Final Idempotency Correction § 5/6: before that staleness check, a same-idempotency-key
 * lookup either relays an in-flight sibling ({@code 409 OPERATION_IN_PROGRESS} — regenerate never
 * returns {@code 202}) or replays a terminal one (success: the same revision; failure: the exact
 * original exception, reconstructed via {@link RegenerateFailureReplay} so the client sees the
 * same status/body, not a redispatch). The coordinator itself writes the {@code IdempotencyRecord}
 * atomically with the durable outcome (success or failure) — this handler no longer writes one
 * after the fact.
 */
public class RegenerateAssessmentDraftHandler implements RegenerateAssessmentDraftUseCase {

    private static final String OPERATION_TYPE = "REGENERATE_REVISION";

    private final AssessmentRepositoryPort assessmentRepository;
    private final AssessmentBriefRepositoryPort assessmentBriefRepository;
    private final AssessmentRevisionRepositoryPort assessmentRevisionRepository;
    private final AiOperationRepositoryPort aiOperationRepository;
    private final AgentAttemptRepositoryPort agentAttemptRepository;
    private final OwnershipVerifier ownershipVerifier;
    private final IdempotencyGuard idempotencyGuard;
    private final AiOperationCoordinator aiOperationCoordinator;

    public RegenerateAssessmentDraftHandler(AssessmentRepositoryPort assessmentRepository,
                                             AssessmentBriefRepositoryPort assessmentBriefRepository,
                                             AssessmentRevisionRepositoryPort assessmentRevisionRepository,
                                             AiOperationRepositoryPort aiOperationRepository,
                                             AgentAttemptRepositoryPort agentAttemptRepository,
                                             OwnershipVerifier ownershipVerifier,
                                             IdempotencyGuard idempotencyGuard,
                                             AiOperationCoordinator aiOperationCoordinator) {
        this.assessmentRepository = assessmentRepository;
        this.assessmentBriefRepository = assessmentBriefRepository;
        this.assessmentRevisionRepository = assessmentRevisionRepository;
        this.aiOperationRepository = aiOperationRepository;
        this.agentAttemptRepository = agentAttemptRepository;
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

        Optional<GenerateAssessmentDraftResult> recovered =
                recoverFromExistingOperation(assessmentId, scope, payloadHash, command.idempotencyKey());
        if (recovered.isPresent()) {
            return recovered.get();
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

        IdempotencyCompletionContext idempotencyContext =
                new IdempotencyCompletionContext(scope, OPERATION_TYPE, command.idempotencyKey(), payloadHash);

        AssessmentRevision revision = aiOperationCoordinator.regenerateRevision(assessment, expectedRevision,
                agentCommand, command.teacherUid(), command.adjustmentNotes(), command.idempotencyKey(), idempotencyContext);

        return GenerateAssessmentDraftResult.fromRevision(revision);
    }

    /**
     * A3 Final Idempotency Correction § 5/6: no {@code IdempotencyRecord} was found for this key.
     * If the latest {@code AiOperation} for this assessment carries the SAME idempotency key,
     * relay its state instead of dispatching a second time: still in flight &rarr; {@code 409
     * OPERATION_IN_PROGRESS} (regenerate never returns {@code 202}); terminal &rarr; reconstruct
     * the original outcome and backfill the missing record. A different (or absent) key falls
     * through to the normal staleness-check/dispatch path, unchanged — concurrent regenerations
     * under distinct keys keep the existing {@code expectedRevisionId} + CAS protection.
     */
    private Optional<GenerateAssessmentDraftResult> recoverFromExistingOperation(
            AssessmentId assessmentId, IdempotencyScope scope, String payloadHash, String idempotencyKey) {
        Optional<AiOperation> existing = aiOperationRepository
                .findLatestByAssessmentIdAndOperationType(assessmentId, AiOperationType.REGENERATE_REVISION);
        if (existing.isEmpty() || !existing.get().getIdempotencyKey().equals(idempotencyKey)) {
            return Optional.empty();
        }

        AiOperation operation = existing.get();
        if (operation.getStatus() == AiOperationStatus.PENDING || operation.getStatus() == AiOperationStatus.IN_PROGRESS) {
            throw new OperationInProgressException(assessmentId.value().toString());
        }

        if (operation.getStatus() == AiOperationStatus.SUCCEEDED) {
            AssessmentRevision revision = assessmentRevisionRepository.findById(operation.getResultRevisionId())
                    .orElseThrow(() -> new ResourceNotFoundException(operation.getResultRevisionId().toString()));
            backfillIdempotencyRecord(scope, idempotencyKey, payloadHash, revision.getId().toString(), 201);
            return Optional.of(GenerateAssessmentDraftResult.fromRevision(revision));
        }

        // FAILED_RETRYABLE / FAILED_TERMINAL — reconstruct and throw the original failure so the
        // client sees the same status/body; do not redispatch to agents/.
        AgentAttempt latestAttempt = agentAttemptRepository.findAllByAiOperationId(operation.getId()).get(0);
        backfillIdempotencyRecord(scope, idempotencyKey, payloadHash, operation.getId().toString(),
                RegenerateFailureReplay.httpStatus(latestAttempt.getFailureCode()));
        throw RegenerateFailureReplay.reconstruct(assessmentId.value().toString(), latestAttempt.getFailureCode());
    }

    /**
     * Best-effort: another concurrent recovery for the same key may have already backfilled this
     * exact record, in which case {@code idempotency_records}' own unique constraint rejects the
     * duplicate insert — harmless, since the outcome already being returned/thrown is correct
     * regardless.
     */
    private void backfillIdempotencyRecord(IdempotencyScope scope, String idempotencyKey, String payloadHash,
                                            String resultReference, int responseStatus) {
        try {
            idempotencyGuard.record(scope, OPERATION_TYPE, idempotencyKey, payloadHash, resultReference, responseStatus);
        } catch (DataIntegrityViolationException ex) {
            // Already backfilled by a concurrent recovery — nothing further to do.
        }
    }

    private GenerateAssessmentDraftResult replay(IdempotencyRecord record) {
        if (record.getResponseStatus() != null && record.getResponseStatus() == 201) {
            UUID revisionId = UUID.fromString(record.getResultReference());
            AssessmentRevision revision = assessmentRevisionRepository.findById(revisionId)
                    .orElseThrow(() -> new ResourceNotFoundException(revisionId.toString()));
            return GenerateAssessmentDraftResult.fromRevision(revision);
        }
        UUID operationId = UUID.fromString(record.getResultReference());
        AiOperation operation = aiOperationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException(operationId.toString()));
        AgentAttempt latestAttempt = agentAttemptRepository.findAllByAiOperationId(operationId).get(0);
        throw RegenerateFailureReplay.reconstruct(operation.getAssessmentId().value().toString(), latestAttempt.getFailureCode());
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
