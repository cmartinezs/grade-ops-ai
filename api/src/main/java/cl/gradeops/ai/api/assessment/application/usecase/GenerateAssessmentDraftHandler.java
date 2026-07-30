package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.agentclient.AssessmentCommand;
import cl.gradeops.ai.api.assessment.application.command.GenerateAssessmentDraftCommand;
import cl.gradeops.ai.api.assessment.application.exception.AlreadyGeneratedException;
import cl.gradeops.ai.api.assessment.application.port.in.GenerateAssessmentDraftUseCase;
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
 * <p>The write path: checks idempotency (Task 06) &rarr; checks the {@code ALREADY_GENERATED}
 * precondition &rarr; delegates to the durable {@link AiOperationCoordinator} (Task 07B). A
 * legacy {@link Assessment} restored with {@code currentRevisionId == null} is treated as "not
 * generated yet," never as an error — this is what lets a pre-cut assessment go through initial
 * generation successfully.
 */
public class GenerateAssessmentDraftHandler implements GenerateAssessmentDraftUseCase {

    private static final String OPERATION_TYPE = "CREATE_INITIAL_REVISION";

    private final AssessmentRepositoryPort assessmentRepository;
    private final AssessmentBriefRepositoryPort assessmentBriefRepository;
    private final AssessmentRevisionRepositoryPort assessmentRevisionRepository;
    private final OwnershipVerifier ownershipVerifier;
    private final IdempotencyGuard idempotencyGuard;
    private final AiOperationCoordinator aiOperationCoordinator;

    public GenerateAssessmentDraftHandler(AssessmentRepositoryPort assessmentRepository,
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
    public GenerateAssessmentDraftResult execute(GenerateAssessmentDraftCommand command) {
        AssessmentId assessmentId = new AssessmentId(command.assessmentId());
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException(assessmentId.value().toString()));
        ownershipVerifier.verify(assessment.getTeacherUid(), command.teacherUid(), assessmentId.value().toString());

        IdempotencyScope scope = IdempotencyScope.assessment(assessmentId.value());
        String payloadHash = IdempotencyPayloadHasher.hash(assessmentId.value().toString());
        Optional<IdempotencyRecord> prior =
                idempotencyGuard.check(scope, OPERATION_TYPE, command.idempotencyKey(), payloadHash);
        if (prior.isPresent()) {
            return replay(prior.get());
        }

        AssessmentBrief brief = assessmentBriefRepository.findByAssessmentId(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException(assessmentId.value().toString()));

        if (assessment.getCurrentRevisionId() != null) {
            throw new AlreadyGeneratedException(assessmentId.value().toString(), assessment.getCurrentRevisionId());
        }

        AssessmentCommand agentCommand = new AssessmentCommand(
                brief.getLearningGoal(), brief.getTopic(), brief.getLevel(), brief.getDuration(), brief.getLanguage(),
                null, null, null, null, null);

        AssessmentRevision revision = aiOperationCoordinator.createInitialRevision(
                assessment, agentCommand, command.teacherUid(), command.idempotencyKey());

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
}
