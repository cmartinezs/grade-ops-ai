package cl.gradeops.ai.agents.assessment.infrastructure.adapter.in.web.response;

import static org.assertj.core.api.Assertions.assertThat;

import cl.gradeops.ai.agents.assessment.application.result.AgentExecutionLogPayload;
import cl.gradeops.ai.agents.assessment.application.result.AssessmentExecutionOutcome;
import cl.gradeops.ai.agents.assessment.application.result.AssessmentResult;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Contract test for {@code POST /internal/agents/assessment}'s response shape — the exact
 * artifact API's Task 07B/07C consume. Uses Spring Boot's auto-configured Jackson 3 {@link
 * JsonMapper} bean (via {@code @JsonTest}, the lightest slice that provides it), never a
 * manually-built mapper, so this proves what {@code agents/} actually serializes in production,
 * not a hand-simulated approximation of it.
 *
 * <p>{@code fixtures/assessment-execution-response.json} is the checked-in fixture API's session
 * consumes. This test fails if either side drifts from the other: a DTO field added, removed, or
 * renamed without updating the fixture, or a hand-edited fixture that no longer matches what the
 * real DTO produces.
 */
@JsonTest
class AssessmentExecutionResponseContractTest {

    private static final String FIXTURE_PATH = "fixtures/assessment-execution-response.json";

    @Autowired
    private JsonMapper jsonMapper;

    private static AssessmentExecutionResponse sampleSuccessfulResponse() {
        AssessmentResult result = AssessmentResult.builder()
                .title("Iterative Array Traversal Exercise")
                .context("Practice iterative traversal and accumulation over arrays.")
                .instructions("Implement a program that reads an integer array and prints the sum "
                        + "and the maximum value using only iterative loops.")
                .objectives(List.of(
                        "Use for/while loops correctly",
                        "Accumulate a running sum",
                        "Track a running maximum"))
                .deliverables(List.of("Source code file", "Sample input/output demonstration"))
                .constraints(List.of("No external libraries", "No recursion"))
                .build();

        AgentExecutionLogPayload log = AgentExecutionLogPayload.builder()
                .agentExecutionId(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"))
                .agentName("assessment")
                .provider("gemini")
                .model("gemini-2.0-flash")
                .promptVersion("// assessment-generation.v1")
                .inputHash("9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08")
                .outputHash("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
                .estimatedInputTokens(120)
                .estimatedOutputTokens(80)
                .costEstimate(0.0084)
                .status("COMPLETED")
                .errorCode(null)
                .startedAt(Instant.parse("2026-07-28T18:40:00Z"))
                .finishedAt(Instant.parse("2026-07-28T18:40:02Z"))
                .build();

        return AssessmentExecutionResponse.from(
                AssessmentExecutionOutcome.builder().result(result).log(log).build());
    }

    @Test
    void serializesProviderAndModelOnASuccessfulResponse() {
        String json = jsonMapper.writeValueAsString(sampleSuccessfulResponse());
        JsonNode actual = jsonMapper.readTree(json);

        assertThat(actual.path("log").path("provider").asString()).isEqualTo("gemini");
        assertThat(actual.path("log").path("model").asString()).isEqualTo("gemini-2.0-flash");
        // Jackson 3's built-in java.time support (no manually-registered JavaTimeModule) renders
        // Instant as an ISO-8601 UTC string, not a numeric timestamp.
        assertThat(actual.path("log").path("startedAt").asString()).isEqualTo("2026-07-28T18:40:00Z");
        assertThat(actual.path("log").path("finishedAt").asString()).isEqualTo("2026-07-28T18:40:02Z");
    }

    @Test
    void matchesTheFixtureConsumedByApi() throws IOException {
        JsonNode actual = jsonMapper.valueToTree(sampleSuccessfulResponse());
        JsonNode expectedFixture;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(FIXTURE_PATH)) {
            assertThat(in).as("fixture resource must exist on the test classpath: %s", FIXTURE_PATH)
                    .isNotNull();
            expectedFixture = jsonMapper.readTree(in);
        }

        assertThat(actual).isEqualTo(expectedFixture);
    }
}
