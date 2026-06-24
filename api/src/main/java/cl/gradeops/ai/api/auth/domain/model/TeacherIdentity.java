package cl.gradeops.ai.api.auth.domain.model;

public record TeacherIdentity(
        String uid,
        String email,
        boolean emailVerified,
        String name,
        String signInProvider
) {}
