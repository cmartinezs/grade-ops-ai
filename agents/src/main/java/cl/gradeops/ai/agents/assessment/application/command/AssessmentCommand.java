package cl.gradeops.ai.agents.assessment.application.command;

/**
 * Input contract for the Assessment Agent (US-010/US-011/US-012).
 *
 * <p>Carries the teacher's brief fields for initial draft generation (US-011) and,
 * when {@code adjustmentNotes}/{@code previousDraftId} are present, the additional
 * input for draft regeneration (US-012) — one shared contract instead of a second
 * command/agent. The agent never persists this data; persistence is {@code api/}'s
 * responsibility.
 *
 * <p>This record does not validate its own fields. It is the target of Jackson
 * deserialization at the internal REST endpoint, so a missing or inconsistent field is
 * expected malformed input, not a caller bug. {@code AssessmentAgentService.validate}
 * is the single place that rejects a blank required field or a mismatched
 * {@code adjustmentNotes}/{@code previousDraftId} pairing, with
 * {@code AssessmentAgentException(INVALID_COMMAND)} — the project's own exception type,
 * per {@code 12-excepciones-y-manejo-de-errores.md} — rather than a Java API exception
 * thrown from this constructor.
 *
 * @param learningGoal what the teacher wants to evaluate
 * @param topic programming topic or skill area
 * @param level target level or difficulty
 * @param duration expected assessment duration
 * @param language programming language or pseudocode
 * @param adjustmentNotes free-text regeneration instructions; {@code null} for initial generation
 * @param previousDraftId identifier of the draft version being regenerated; {@code null} for initial generation
 */
public record AssessmentCommand(
        String learningGoal,
        String topic,
        String level,
        String duration,
        String language,
        String adjustmentNotes,
        String previousDraftId) {
}
