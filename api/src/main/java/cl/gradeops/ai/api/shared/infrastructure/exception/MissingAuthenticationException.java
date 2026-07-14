package cl.gradeops.ai.api.shared.infrastructure.exception;

/**
 * Thrown when a controller method that requires an authenticated principal is reached with no
 * authentication set on the security context — should never happen given the security filter
 * chain's own guarantees, but is treated as an internal error rather than an unchecked NPE.
 */
public class MissingAuthenticationException extends InfrastructureException {
    public MissingAuthenticationException() {
        super("No authenticated teacher in security context");
    }
}
