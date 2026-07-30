package cl.gradeops.ai.api.assessment.application.result;

/**
 * {@code POST .../draft}'s two legitimate outcomes (A3 Contract Correction § Correction 2):
 * either the synchronous dispatch succeeded and produced a new {@code AssessmentRevision} ({@code
 * 201}), or a durable {@code AiOperation}/{@code AgentAttempt} pair was recorded before dispatch
 * and the dispatch itself failed ({@code 202}) — the operation record having been created
 * durably is what makes this a legitimate "accepted" response rather than a bare error, per the
 * Durable AI Operation Model ADR. Regenerate is unaffected — it keeps its {@code 201}-or-typed-
 * error contract unchanged, since it never leaves the assessment revision-less to begin with.
 */
public sealed interface GenerateAssessmentDraftOutcome {

    record RevisionCreated(GenerateAssessmentDraftResult revision) implements GenerateAssessmentDraftOutcome {}

    record OperationAccepted(RetryGenerationResult operation) implements GenerateAssessmentDraftOutcome {}
}
