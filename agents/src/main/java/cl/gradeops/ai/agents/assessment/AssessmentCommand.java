package cl.gradeops.ai.agents.assessment;

import static java.util.Objects.requireNonNull;

/**
 * Input contract for the Assessment Agent (US-010/US-011/US-012).
 *
 * <p>Carries the teacher's brief fields for initial draft generation (US-011) and,
 * when {@code adjustmentNotes}/{@code previousDraftId} are present, the additional
 * input for draft regeneration (US-012) — one shared contract instead of a second
 * command/agent. The agent never persists this data; persistence is {@code api/}'s
 * responsibility.
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

    public AssessmentCommand {
        requireNonNull(learningGoal, "learningGoal is required");
        requireNonNull(topic, "topic is required");
        requireNonNull(level, "level is required");
        requireNonNull(duration, "duration is required");
        requireNonNull(language, "language is required");

        if ((adjustmentNotes == null) != (previousDraftId == null)) {
            throw new IllegalArgumentException(
                    "adjustmentNotes and previousDraftId must be provided together for regeneration");
        }
    }
}
