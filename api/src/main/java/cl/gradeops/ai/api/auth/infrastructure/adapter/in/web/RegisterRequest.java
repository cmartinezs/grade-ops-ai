package cl.gradeops.ai.api.auth.infrastructure.adapter.in.web;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank String idToken,
        String firstName,
        String lastName
) {}
