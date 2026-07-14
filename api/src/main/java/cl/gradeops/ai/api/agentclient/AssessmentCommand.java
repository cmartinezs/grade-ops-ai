package cl.gradeops.ai.api.agentclient;

/**
 * Field-for-field mirror of {@code agents/}'s {@code AssessmentCommand} (verified 2026-07-13
 * against {@code agents/src/main/java/.../assessment/application/command/AssessmentCommand.java}).
 * Kept as a separate copy rather than a shared library, since {@code api/} and {@code agents/}
 * deploy independently.
 *
 * <p>{@code adjustmentNotes}/{@code previousDraftId}/{@code previousDraft} are only present for
 * regeneration (US-012); all three are {@code null} for initial generation (US-011).
 * {@code previousDraftId} is for correlation/audit only — {@code agents/} never persists data or
 * calls back into {@code api/}, so {@code api/} must resolve the prior draft's content on its own
 * side and send it as {@code previousDraft}.
 *
 * <p>{@code provider}/{@code model} are optional per-request overrides; {@code null} lets
 * {@code agents/} apply its own configured defaults.
 */
public record AssessmentCommand(
        String learningGoal,
        String topic,
        String level,
        String duration,
        String language,
        String adjustmentNotes,
        String previousDraftId,
        String previousDraft,
        String provider,
        String model) {
}
