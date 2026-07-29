package cl.gradeops.ai.api.agentclient;

import cl.gradeops.ai.api.assessment.domain.model.AgentAttempt;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Task 07C — proves {@code api/}'s {@code agentclient} module correctly deserializes the
 * {@code provider} field from Agents' own real fixture (copied verbatim from
 * {@code origin/feat/assessment-authoring-operation-foundation-agents}, not a hand-rolled JSON
 * string), using Spring Boot's auto-configured Jackson 3 {@link JsonMapper} bean via
 * {@code @JsonTest} — the same slice Agents' own contract test uses, per
 * {@code JacksonConfig.java}'s precedent (no second, manually-built mapper).
 */
@JsonTest
class AssessmentAgentResponseContractTest {

    private static final String FIXTURE_PATH = "fixtures/agents/assessment-execution-response.json";

    @Autowired
    private JsonMapper jsonMapper;

    private AssessmentAgentResponse readFixture() throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(FIXTURE_PATH)) {
            assertThat(in).as("fixture resource must exist on the test classpath: %s", FIXTURE_PATH).isNotNull();
            return jsonMapper.readValue(in, AssessmentAgentResponse.class);
        }
    }

    @Test
    void shouldDeserializeProviderAndModelFromAgentsFixture() throws IOException {
        AssessmentAgentResponse response = readFixture();

        assertThat(response.log().provider()).isEqualTo("gemini");
        assertThat(response.log().model()).isEqualTo("gemini-2.0-flash");
    }

    @Test
    void shouldDeserializeAgentExecutionIdAndTimestampsFromAgentsFixture() throws IOException {
        AssessmentAgentResponse response = readFixture();

        assertThat(response.log().agentExecutionId()).isEqualTo(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"));
        assertThat(response.log().startedAt()).isEqualTo(Instant.parse("2026-07-28T18:40:00Z"));
        assertThat(response.log().finishedAt()).isEqualTo(Instant.parse("2026-07-28T18:40:02Z"));
    }

    @Test
    void shouldFlowDeserializedProviderIntoAgentAttemptResolvedProvider() throws IOException {
        AssessmentAgentResponse response = readFixture();

        AgentAttempt attempt = AgentAttempt.dispatch(UUID.randomUUID(), 1, "assessment", null, "corr-1")
                .markCompleted(response.log().provider(), response.log().model(), null,
                        response.log().estimatedInputTokens(), response.log().estimatedOutputTokens(),
                        null, null);

        assertThat(attempt.getResolvedProvider()).isEqualTo("gemini");
        assertThat(attempt.getResolvedModel()).isEqualTo("gemini-2.0-flash");
    }
}
