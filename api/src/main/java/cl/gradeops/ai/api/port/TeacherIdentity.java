package cl.gradeops.ai.api.port;

public record TeacherIdentity(
        String uid,
        String email,
        boolean emailVerified,
        String name,
        String signInProvider   // "GOOGLE" or "EMAIL_PASSWORD"
) {}
