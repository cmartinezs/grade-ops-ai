package cl.gradeops.ai.agents.assessment.application.orchestrator;

import cl.gradeops.ai.agents.assessment.application.command.AssessmentCommand;
import cl.gradeops.ai.agents.assessment.application.exception.AssessmentAgentException;
import cl.gradeops.ai.agents.assessment.application.exception.AssessmentAgentException.Reason;
import cl.gradeops.ai.agents.assessment.application.port.out.AssessmentGenerationPort;
import cl.gradeops.ai.agents.assessment.application.port.out.AssessmentGenerationResponse;
import cl.gradeops.ai.agents.assessment.application.result.AgentExecutionLogPayload;
import cl.gradeops.ai.agents.assessment.application.result.AssessmentExecutionOutcome;
import cl.gradeops.ai.agents.assessment.application.result.AssessmentResult;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.stringtemplate.v4.ST;

/**
 * Runs the fixed agent pipeline (`CLAUDE.md`): validate command → load data → build envelope →
 * call Gemini → validate structured output → log execution → return result. Never touches
 * Spring AI directly — {@code assessmentGenerationPort} is the only collaborator.
 *
 * <p>The template body is cached as an immutable {@code String} (loaded once via {@link
 * #loadTemplate()}), not a single shared {@code ST} instance — {@code ST.add(...)} mutates
 * instance state, so reusing one {@code ST} object across concurrent requests on this
 * singleton bean would corrupt renders. Each {@link #buildEnvelope(AssessmentCommand)} call
 * constructs a fresh {@code ST} from the cached body.
 */
@RequiredArgsConstructor
public class AssessmentAgentOrchestrator {

    private static final String TEMPLATE_RESOURCE = "prompts/assessment-generation.st";
    private static final String AGENT_NAME = "assessment";

    /** Placeholder blended rate — real per-model Gemini pricing is out of scope for this
     *  hackathon-stage estimate; {@code costEstimate} is explicitly best-effort throughout. */
    private static final double COST_PER_1K_TOKENS = 0.000075;

    private final AssessmentGenerationPort assessmentGenerationPort;

    private String templateBody;
    private String promptVersion;

    @PostConstruct
    void loadTemplate() {
        String raw;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(TEMPLATE_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Prompt template resource not found: " + TEMPLATE_RESOURCE);
            }
            raw = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load prompt template: " + TEMPLATE_RESOURCE, e);
        }
        int firstNewline = raw.indexOf('\n');
        this.promptVersion = raw.substring(0, firstNewline).strip();
        this.templateBody = raw.substring(firstNewline + 1);
    }

    public AssessmentExecutionOutcome generate(AssessmentCommand command) {
        Instant startedAt = Instant.now();
        validate(command, startedAt);

        String renderedPrompt = buildEnvelope(command);
        String inputHash = sha256Hex(renderedPrompt);

        AssessmentGenerationResponse response;
        try {
            response = assessmentGenerationPort.generate(renderedPrompt);
        } catch (RuntimeException e) {
            throw malformedOutput(
                    "Assessment generation response could not be parsed: " + e.getMessage(),
                    inputHash, null, null, null, startedAt);
        }

        validateOutput(response, inputHash, startedAt);

        AssessmentResult result = response.result();
        String outputHash = sha256Hex(response.rawResponseText());
        Double costEstimate = estimateCost(response.estimatedInputTokens(), response.estimatedOutputTokens());

        AgentExecutionLogPayload log = AgentExecutionLogPayload.builder()
                .agentExecutionId(UUID.randomUUID())
                .agentName(AGENT_NAME)
                .model(response.modelName())
                .promptVersion(promptVersion)
                .inputHash(inputHash)
                .outputHash(outputHash)
                .estimatedInputTokens(response.estimatedInputTokens())
                .estimatedOutputTokens(response.estimatedOutputTokens())
                .costEstimate(costEstimate)
                .status("COMPLETED")
                .errorCode(null)
                .startedAt(startedAt)
                .finishedAt(Instant.now())
                .build();

        return AssessmentExecutionOutcome.builder().result(result).log(log).build();
    }

    private void validate(AssessmentCommand command, Instant startedAt) {
        if (isBlank(command.learningGoal())
                || isBlank(command.topic())
                || isBlank(command.level())
                || isBlank(command.duration())
                || isBlank(command.language())) {
            throw invalidCommand("A required AssessmentCommand field is blank", startedAt);
        }

        boolean hasAdjustmentNotes = command.adjustmentNotes() != null;
        boolean hasPreviousDraftId = command.previousDraftId() != null;
        boolean hasPreviousDraft = command.previousDraft() != null;
        if (hasAdjustmentNotes != hasPreviousDraftId || hasPreviousDraftId != hasPreviousDraft) {
            throw invalidCommand(
                    "adjustmentNotes, previousDraftId, and previousDraft must be all present or all absent",
                    startedAt);
        }
    }

    private String buildEnvelope(AssessmentCommand command) {
        ST template = new ST(templateBody);
        template.add("learningGoal", command.learningGoal());
        template.add("topic", command.topic());
        template.add("level", command.level());
        template.add("duration", command.duration());
        template.add("language", command.language());
        template.add("adjustmentNotes", command.adjustmentNotes());
        template.add("previousDraft", command.previousDraft());
        return template.render();
    }

    private void validateOutput(AssessmentGenerationResponse response, String inputHash, Instant startedAt) {
        AssessmentResult result = response.result();
        if (result == null
                || isBlank(result.title())
                || isBlank(result.context())
                || isBlank(result.instructions())
                || result.objectives().isEmpty()
                || result.deliverables().isEmpty()
                || result.constraints().isEmpty()) {
            throw malformedOutput(
                    "Assessment generation response is missing a required field",
                    inputHash,
                    response.modelName(),
                    response.estimatedInputTokens(),
                    response.estimatedOutputTokens(),
                    startedAt);
        }
    }

    private AssessmentAgentException invalidCommand(String message, Instant startedAt) {
        AgentExecutionLogPayload log = AgentExecutionLogPayload.builder()
                .agentExecutionId(UUID.randomUUID())
                .agentName(AGENT_NAME)
                .promptVersion(promptVersion)
                .status("FAILED")
                .errorCode(Reason.INVALID_COMMAND.name())
                .startedAt(startedAt)
                .finishedAt(Instant.now())
                .build();
        return new AssessmentAgentException(Reason.INVALID_COMMAND, message, log);
    }

    private AssessmentAgentException malformedOutput(
            String message,
            String inputHash,
            String model,
            Integer estimatedInputTokens,
            Integer estimatedOutputTokens,
            Instant startedAt) {
        AgentExecutionLogPayload log = AgentExecutionLogPayload.builder()
                .agentExecutionId(UUID.randomUUID())
                .agentName(AGENT_NAME)
                .model(model)
                .promptVersion(promptVersion)
                .inputHash(inputHash)
                .estimatedInputTokens(estimatedInputTokens)
                .estimatedOutputTokens(estimatedOutputTokens)
                .costEstimate(estimateCost(estimatedInputTokens, estimatedOutputTokens))
                .status("FAILED")
                .errorCode(Reason.MALFORMED_OUTPUT.name())
                .startedAt(startedAt)
                .finishedAt(Instant.now())
                .build();
        return new AssessmentAgentException(Reason.MALFORMED_OUTPUT, message, log);
    }

    private static Double estimateCost(Integer estimatedInputTokens, Integer estimatedOutputTokens) {
        if (estimatedInputTokens == null || estimatedOutputTokens == null) {
            return null;
        }
        return (estimatedInputTokens + estimatedOutputTokens) / 1000.0 * COST_PER_1K_TOKENS;
    }

    private static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
