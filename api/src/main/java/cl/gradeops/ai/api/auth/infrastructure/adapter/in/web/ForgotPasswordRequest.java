package cl.gradeops.ai.api.auth.infrastructure.adapter.in.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
    @NotBlank(message = "El correo es requerido.")
    @Email(message = "Ingresa una dirección de correo válida.")
    String email
) {}
