package cl.gradeops.ai.api.common;

public class DuplicateEmailException extends RuntimeException {

    private final String email;

    public DuplicateEmailException(String email) {
        super("Teacher with email already exists: " + email);
        this.email = email;
    }

    public String getEmail() { return email; }
}
