package cl.gradeops.ai.api.shared.infrastructure.adapter.in.web;

import cl.gradeops.ai.api.auth.domain.exception.InvalidResetCodeException;
import cl.gradeops.ai.api.auth.domain.exception.PasswordMismatchException;
import cl.gradeops.ai.api.auth.domain.exception.ResetCodeEmailMismatchException;
import cl.gradeops.ai.api.shared.application.exception.ApplicationException;
import cl.gradeops.ai.api.shared.domain.exception.DomainInvariantViolationException;
import cl.gradeops.ai.api.shared.domain.exception.DuplicateEmailException;
import cl.gradeops.ai.api.shared.domain.exception.InvalidTokenException;
import cl.gradeops.ai.api.shared.domain.exception.ResourceNotFoundException;
import cl.gradeops.ai.api.shared.infrastructure.exception.InfrastructureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ── Excepciones de dominio específicas ────────────────────────────────────

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateEmail(DuplicateEmailException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of("EMAIL_ALREADY_EXISTS", ex.getEmail()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of("NOT_FOUND", ex.getResourceId()));
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidToken(InvalidTokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiErrorResponse.of("INVALID_TOKEN"));
    }

    @ExceptionHandler(InvalidResetCodeException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidResetCode(InvalidResetCodeException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(ApiErrorResponse.of("INVALID_RESET_CODE"));
    }

    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handlePasswordMismatch(PasswordMismatchException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(ApiErrorResponse.of("PASSWORD_MISMATCH"));
    }

    @ExceptionHandler(ResetCodeEmailMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleResetCodeEmailMismatch(ResetCodeEmailMismatchException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(ApiErrorResponse.of("RESET_CODE_EMAIL_MISMATCH"));
    }

    // ── Raíces de jerarquía propia ────────────────────────────────────────────

    @ExceptionHandler(DomainInvariantViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDomainInvariant(DomainInvariantViolationException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(ApiErrorResponse.of("DOMAIN_INVARIANT_VIOLATION", ex.getMessage()));
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiErrorResponse> handleApplication(ApplicationException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(ApiErrorResponse.of("APPLICATION_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(InfrastructureException.class)
    public ResponseEntity<ApiErrorResponse> handleInfrastructure(InfrastructureException ex) {
        log.warn("Infrastructure failure: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of("INFRASTRUCTURE_ERROR"));
    }

    // ── Validación HTTP ───────────────────────────────────────────────────────

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of("MALFORMED_REQUEST"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<FieldErrorResponse>> handleValidation(MethodArgumentNotValidException ex) {
        List<FieldErrorResponse> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new FieldErrorResponse(e.getField(), e.getDefaultMessage()))
                .toList();
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(errors);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingParam(MissingServletRequestParameterException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of("MISSING_PARAMETER", ex.getParameterName()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(ResponseStatusException ex) {
        String errorCode = ex.getReason() != null ? ex.getReason() : ex.getStatusCode().toString();
        return ResponseEntity.status(ex.getStatusCode()).body(ApiErrorResponse.of(errorCode));
    }

    // ── Catch-all para errores no previstos ───────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of("INTERNAL_ERROR"));
    }
}
