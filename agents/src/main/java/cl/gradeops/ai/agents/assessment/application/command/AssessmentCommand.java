package cl.gradeops.ai.agents.assessment.application.command;

import lombok.Builder;

/**
 * Input contract for the Assessment Agent (US-010/US-011/US-012).
 *
 * <p>Carries the teacher's brief fields for initial draft generation (US-011) and,
 * when {@code adjustmentNotes}/{@code previousDraftId}/{@code previousDraft} are present, the
 * additional input for draft regeneration (US-012) — one shared contract instead of a second
 * command/agent. The agent never persists this data; persistence is {@code api/}'s
 * responsibility. Because this artifact never persists anything and never calls back into
 * {@code api/}, it cannot resolve {@code previousDraftId} into content on its own —
 * {@code api/} must resolve the ID on its side and send the actual prior-draft content as
 * {@code previousDraft}; {@code previousDraftId} exists purely for correlation/audit
 * (e.g. {@code AgentExecutionLogPayload}), not for content lookup.
 *
 * <p>This record does not validate its own fields. It is the target of Jackson
 * deserialization at the internal REST endpoint, so a missing or inconsistent field is
 * expected malformed input, not a caller bug. {@code AssessmentAgentOrchestrator.validate}
 * is the single place that rejects a blank required field or an inconsistent regeneration
 * triple ({@code adjustmentNotes}/{@code previousDraftId}/{@code previousDraft} must all be
 * present together or all absent), with {@code AssessmentAgentException(INVALID_COMMAND)} —
 * the project's own exception type — rather than a Java API exception thrown from this
 * constructor.
 *
 * @param learningGoal what the teacher wants to evaluate
 * @param topic programming topic or skill area
 * @param level target level or difficulty
 * @param duration expected assessment duration
 * @param language programming language or pseudocode
 * @param adjustmentNotes free-text regeneration instructions; {@code null} for initial generation
 * @param previousDraftId identifier of the draft version being regenerated, for correlation/audit
 *     only; {@code null} for initial generation
 * @param previousDraft rendered content/summary of the prior draft being regenerated, sent by
 *     {@code api/} (which owns the actual persisted draft); {@code null} for initial generation
 * @param provider which LLM provider generates this draft; {@code null} selects the configured
 *     default. An unrecognized value is rejected by {@code AssessmentAgentOrchestrator.validate}
 *     with {@code AssessmentAgentException(INVALID_COMMAND)}, the same as any other invalid field.
 * @param model a literal, provider-specific model name, forwarded as a per-call option to the
 *     resolved provider's {@code ChatClient}; {@code null} lets that provider's own configured
 *     default apply. Not validated against a list of models the provider actually supports — an
 *     unsupported value surfaces as whatever error the provider itself returns, translated by the
 *     orchestrator's existing generation-failure handling. Which models each provider actually
 *     offers, and which are appropriate for which capability, is not tracked anywhere yet (see
 *     this feature's own residual notes) — the caller is responsible for sending a value the
 *     resolved provider recognizes.
 */
@Builder
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
