package cl.gradeops.ai.api.auth;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank String idToken,
        String firstName,
        String lastName
) {}
