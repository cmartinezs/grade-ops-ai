package cl.gradeops.ai.api.auth.infrastructure.adapter.in.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
    @NotBlank String code,

    @NotBlank(message = "El correo es requerido.")
    @Email(message = "Ingresa una dirección de correo válida.")
    String email,

    @NotBlank
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres.")
    String password,

    @NotBlank(message = "Repite la contraseña.")
    String passwordRepeat
) {}
