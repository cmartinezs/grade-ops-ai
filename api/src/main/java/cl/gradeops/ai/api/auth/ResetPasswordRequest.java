package cl.gradeops.ai.api.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
    @NotBlank(message = "El correo es requerido.")
    @Email(message = "Ingresa una dirección de correo válida.")
    String email,

    @NotBlank
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres.")
    String password,

    @NotBlank(message = "Repite la contraseña.")
    String passwordRepeat
) {}
