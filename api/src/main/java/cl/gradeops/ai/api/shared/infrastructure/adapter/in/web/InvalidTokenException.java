package cl.gradeops.ai.api.shared.infrastructure.adapter.in.web;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) { super(message); }
}
