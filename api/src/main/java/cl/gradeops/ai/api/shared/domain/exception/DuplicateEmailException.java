package cl.gradeops.ai.api.shared.domain.exception;

public class DuplicateEmailException extends DomainException {

    private final String email;

    public DuplicateEmailException(String email) {
        super("Teacher with email already exists: " + email);
        this.email = email;
    }

    public String getEmail() { return email; }
}
