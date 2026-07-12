package cl.gradeops.ai.agents.shared.infrastructure.adapter.in.web;

import cl.gradeops.ai.agents.assessment.application.exception.AssessmentAgentException;
import cl.gradeops.ai.agents.shared.infrastructure.adapter.in.web.response.AgentErrorResponse;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * The single place any {@code agents/} endpoint's exceptions become HTTP responses. Never
 * exposes a stack trace, a raw prompt, or request/response payload content — only the reason
 * code, message, and the execution-log payload the exception already carries.
 */
@RestControllerAdvice
class AgentGlobalExceptionHandler {

    @ExceptionHandler(AssessmentAgentException.class)
    public ResponseEntity<AgentErrorResponse> handleAssessmentAgentException(AssessmentAgentException ex) {
        AgentErrorResponse body = new AgentErrorResponse(
                ex.reason().name(), ex.getMessage(), ex.log(), MDC.get(CorrelationIdFilter.MDC_KEY));
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(body);
    }
}
