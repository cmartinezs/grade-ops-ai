package cl.gradeops.ai.api.agentclient;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AssessmentAgentClientTest {

    private static final String AGENT_URL = "http://agents.test/internal/agents/assessment";
    private static final String CORRELATION_ID = "test-correlation-id";

    private static final AssessmentCommand COMMAND = new AssessmentCommand(
            "Evaluate loops", "Java loops", "basic", "90min", "Java",
            null, null, null, null, null);

    private final RestClient.Builder restClientBuilder = RestClient.builder().baseUrl("http://agents.test");
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
    private final JsonMapper jsonMapper = JsonMapper.builder().build();
    private final AssessmentAgentClient client = new AssessmentAgentClient(restClientBuilder.build(), jsonMapper);

    @Test
    void shouldMapSuccessfulResponseToAssessmentAgentResponse() {
        server.expect(requestTo(AGENT_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Correlation-Id", CORRELATION_ID))
                .andRespond(withSuccess("""
                        {
                          "result": {
                            "title": "Java Loops Quiz",
                            "context": "Second-semester students",
                            "instructions": "Solve the following...",
                            "objectives": ["Evaluate loop control flow"],
                            "deliverables": ["Working program"],
                            "constraints": ["No external libraries"]
                          },
                          "log": {
                            "agentExecutionId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                            "agentName": "assessment",
                            "provider": "gemini",
                            "model": "gemini-2.0-flash",
                            "promptVersion": "v1",
                            "inputHash": "abc123",
                            "outputHash": "def456",
                            "estimatedInputTokens": 120,
                            "estimatedOutputTokens": 340,
                            "costEstimate": 0.0042,
                            "status": "COMPLETED",
                            "errorCode": null,
                            "startedAt": "2026-07-13T10:00:00Z",
                            "finishedAt": "2026-07-13T10:00:02Z"
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        AssessmentAgentResponse response = client.generate(COMMAND, CORRELATION_ID);

        assertThat(response.result().title()).isEqualTo("Java Loops Quiz");
        assertThat(response.result().objectives()).containsExactly("Evaluate loop control flow");
        assertThat(response.log().agentName()).isEqualTo("assessment");
        assertThat(response.log().provider()).isEqualTo("gemini");
        assertThat(response.log().status()).isEqualTo("COMPLETED");
        assertThat(response.log().errorCode()).isNull();
        assertThat(response.log().estimatedInputTokens()).isEqualTo(120);
        server.verify();
    }

    @Test
    void shouldMapClientErrorResponseToAgentRejectedWhenBodyIsNotAgentsErrorShape() {
        server.expect(requestTo(AGENT_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatusCode.valueOf(422))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"something else\"}"));

        assertThatThrownBy(() -> client.generate(COMMAND, CORRELATION_ID))
                .isInstanceOf(AgentClientException.class)
                .extracting(ex -> ((AgentClientException) ex).reason())
                .isEqualTo(AgentClientException.Reason.AGENT_REJECTED);
    }

    @Test
    void shouldCaptureAgentsSideDetailWhenBodyIsInvalidCommandShape() {
        server.expect(requestTo(AGENT_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatusCode.valueOf(422))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                {
                                  "errorCode": "INVALID_COMMAND",
                                  "message": "blank learningGoal",
                                  "log": {
                                    "agentExecutionId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                    "agentName": "assessment",
                                    "provider": null,
                                    "model": null,
                                    "promptVersion": null,
                                    "inputHash": null,
                                    "outputHash": null,
                                    "estimatedInputTokens": null,
                                    "estimatedOutputTokens": null,
                                    "costEstimate": null,
                                    "status": "FAILED",
                                    "errorCode": "INVALID_COMMAND",
                                    "startedAt": "2026-07-13T10:00:00Z",
                                    "finishedAt": "2026-07-13T10:00:00Z"
                                  },
                                  "correlationId": "agents-corr-1"
                                }
                                """));

        assertThatThrownBy(() -> client.generate(COMMAND, CORRELATION_ID))
                .isInstanceOf(AgentClientException.class)
                .satisfies(ex -> {
                    AgentClientException agentEx = (AgentClientException) ex;
                    assertThat(agentEx.reason()).isEqualTo(AgentClientException.Reason.AGENT_REJECTED);
                    assertThat(agentEx.agentError()).isNotNull();
                    assertThat(agentEx.agentError().errorCode()).isEqualTo("INVALID_COMMAND");
                    assertThat(agentEx.agentError().log().provider()).isNull();
                });
    }

    @Test
    void shouldCaptureAgentsSideDetailAndProviderWhenBodyIsMalformedOutputShape() {
        server.expect(requestTo(AGENT_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatusCode.valueOf(422))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                {
                                  "errorCode": "MALFORMED_OUTPUT",
                                  "message": "could not parse model output",
                                  "log": {
                                    "agentExecutionId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                    "agentName": "assessment",
                                    "provider": "groq",
                                    "model": "llama-3",
                                    "promptVersion": "v1",
                                    "inputHash": "abc",
                                    "outputHash": null,
                                    "estimatedInputTokens": 100,
                                    "estimatedOutputTokens": null,
                                    "costEstimate": null,
                                    "status": "FAILED",
                                    "errorCode": "MALFORMED_OUTPUT",
                                    "startedAt": "2026-07-13T10:00:00Z",
                                    "finishedAt": "2026-07-13T10:00:00Z"
                                  },
                                  "correlationId": "agents-corr-2"
                                }
                                """));

        assertThatThrownBy(() -> client.generate(COMMAND, CORRELATION_ID))
                .isInstanceOf(AgentClientException.class)
                .satisfies(ex -> {
                    AgentClientException agentEx = (AgentClientException) ex;
                    assertThat(agentEx.agentError().errorCode()).isEqualTo("MALFORMED_OUTPUT");
                    assertThat(agentEx.agentError().log().provider()).isEqualTo("groq");
                });
    }

    @Test
    void shouldMapServerErrorResponseToAgentError() {
        server.expect(requestTo(AGENT_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatusCode.valueOf(500))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"internal\"}"));

        assertThatThrownBy(() -> client.generate(COMMAND, CORRELATION_ID))
                .isInstanceOf(AgentClientException.class)
                .extracting(ex -> ((AgentClientException) ex).reason())
                .isEqualTo(AgentClientException.Reason.AGENT_ERROR);
    }

    @Test
    void shouldMapConnectionFailureToUnreachable() {
        RestClient unreachableClient = RestClient.builder().baseUrl("http://127.0.0.1:59999").build();
        AssessmentAgentClient unreachableAgentClient = new AssessmentAgentClient(unreachableClient, jsonMapper);

        assertThatThrownBy(() -> unreachableAgentClient.generate(COMMAND, CORRELATION_ID))
                .isInstanceOf(AgentClientException.class)
                .extracting(ex -> ((AgentClientException) ex).reason())
                .isEqualTo(AgentClientException.Reason.UNREACHABLE);
    }
}
