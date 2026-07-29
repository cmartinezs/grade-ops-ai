package cl.gradeops.ai.api.agentclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.json.JsonMapper;

/**
 * The only class that calls {@code agents/}. The caller supplies the correlation id (rather
 * than this class generating one internally) so that the durable {@code AgentAttempt} record
 * created before dispatch (see {@code AiOperationCoordinator}) carries the exact id actually
 * sent over the wire — not a different, unpersisted one.
 */
@Slf4j
public class AssessmentAgentClient {

    private static final String ASSESSMENT_PATH = "/internal/agents/assessment";
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    private final RestClient restClient;
    private final JsonMapper jsonMapper;

    public AssessmentAgentClient(RestClient restClient, JsonMapper jsonMapper) {
        this.restClient = restClient;
        this.jsonMapper = jsonMapper;
    }

    public AssessmentAgentResponse generate(AssessmentCommand command, String correlationId) {
        try {
            ResponseEntity<AssessmentAgentResponse> response = restClient.post()
                    .uri(ASSESSMENT_PATH)
                    .header(CORRELATION_ID_HEADER, correlationId)
                    .body(command)
                    .retrieve()
                    .toEntity(AssessmentAgentResponse.class);
            log.debug("agents/ assessment call completed, correlationId={}",
                    responseCorrelationId(response.getHeaders(), correlationId));
            return response.getBody();
        } catch (RestClientResponseException ex) {
            String responseCorrelationId = responseCorrelationId(ex.getResponseHeaders(), correlationId);
            if (ex.getStatusCode().is4xxClientError()) {
                AssessmentAgentErrorPayload agentError = tryParseAgentError(ex);
                log.warn("agents/ assessment call rejected, status={}, correlationId={}",
                        ex.getStatusCode().value(), responseCorrelationId);
                throw new AgentClientException(AgentClientException.Reason.AGENT_REJECTED,
                        "agents/ call failed with status " + ex.getStatusCode().value()
                                + " (correlationId=" + responseCorrelationId + ")", ex, agentError);
            }
            log.warn("agents/ assessment call errored, status={}, correlationId={}",
                    ex.getStatusCode().value(), responseCorrelationId);
            throw new AgentClientException(AgentClientException.Reason.AGENT_ERROR,
                    "agents/ call failed with status " + ex.getStatusCode().value()
                            + " (correlationId=" + responseCorrelationId + ")", ex);
        } catch (RestClientException ex) {
            log.warn("agents/ assessment call unreachable, correlationId={}", correlationId);
            throw new AgentClientException(AgentClientException.Reason.UNREACHABLE,
                    "agents/ was unreachable (correlationId=" + correlationId + ")", ex);
        }
    }

    /** Best-effort: returns {@code null} when the body isn't agents/'s recognized error shape. */
    private AssessmentAgentErrorPayload tryParseAgentError(RestClientResponseException ex) {
        String body = ex.getResponseBodyAsString();
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            AssessmentAgentErrorPayload parsed = jsonMapper.readValue(body, AssessmentAgentErrorPayload.class);
            if (parsed.errorCode() == null) {
                return null;
            }
            return parsed;
        } catch (RuntimeException ex2) {
            return null;
        }
    }

    private static String responseCorrelationId(HttpHeaders headers, String fallback) {
        String value = headers != null ? headers.getFirst(CORRELATION_ID_HEADER) : null;
        return value != null ? value : fallback;
    }
}
