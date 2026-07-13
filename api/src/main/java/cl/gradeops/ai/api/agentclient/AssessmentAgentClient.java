package cl.gradeops.ai.api.agentclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.UUID;

/**
 * The only class that calls {@code agents/}. Generates a fresh {@code X-Correlation-Id} per
 * call so cross-service log tracing between {@code api/} and {@code agents/} is possible from
 * day one (see {@code CorrelationIdFilter} in {@code agents/}, which honors this header).
 */
public class AssessmentAgentClient {

    private static final Logger log = LoggerFactory.getLogger(AssessmentAgentClient.class);
    private static final String ASSESSMENT_PATH = "/internal/agents/assessment";
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    private final RestClient restClient;

    public AssessmentAgentClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public AssessmentAgentResponse generate(AssessmentCommand command) {
        String correlationId = UUID.randomUUID().toString();
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
            AgentClientException.Reason reason = ex.getStatusCode().is4xxClientError()
                    ? AgentClientException.Reason.AGENT_REJECTED
                    : AgentClientException.Reason.AGENT_ERROR;
            log.warn("agents/ assessment call rejected, status={}, correlationId={}",
                    ex.getStatusCode().value(), responseCorrelationId);
            throw new AgentClientException(reason,
                    "agents/ call failed with status " + ex.getStatusCode().value()
                            + " (correlationId=" + responseCorrelationId + ")", ex);
        } catch (RestClientException ex) {
            log.warn("agents/ assessment call unreachable, correlationId={}", correlationId);
            throw new AgentClientException(AgentClientException.Reason.UNREACHABLE,
                    "agents/ was unreachable (correlationId=" + correlationId + ")", ex);
        }
    }

    private static String responseCorrelationId(HttpHeaders headers, String fallback) {
        String value = headers != null ? headers.getFirst(CORRELATION_ID_HEADER) : null;
        return value != null ? value : fallback;
    }
}
