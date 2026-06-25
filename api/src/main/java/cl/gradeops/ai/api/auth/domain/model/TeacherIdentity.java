package cl.gradeops.ai.api.auth.domain.model;

import java.util.Objects;

public record TeacherIdentity(
        String uid,
        String email,
        boolean emailVerified,
        String name,
        SignInProvider signInProvider
) {
    public TeacherIdentity {
        Objects.requireNonNull(uid, "uid is required");
        Objects.requireNonNull(email, "email is required");
        Objects.requireNonNull(signInProvider, "signInProvider is required");
    }
}
