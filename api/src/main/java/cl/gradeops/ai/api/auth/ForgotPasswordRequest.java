package cl.gradeops.ai.api.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
    @NotBlank(message = "El correo es requerido.")
    @Email(message = "Ingresa una dirección de correo válida.")
    String email
) {}
