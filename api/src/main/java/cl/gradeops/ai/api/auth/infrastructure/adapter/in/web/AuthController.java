package cl.gradeops.ai.api.auth.infrastructure.adapter.in.web;

import cl.gradeops.ai.api.auth.application.command.RegisterCommand;
import cl.gradeops.ai.api.auth.application.command.ResetPasswordCommand;
import cl.gradeops.ai.api.auth.application.command.SendPasswordResetEmailCommand;
import cl.gradeops.ai.api.auth.application.port.in.RegisterUseCase;
import cl.gradeops.ai.api.auth.application.port.in.ResetPasswordUseCase;
import cl.gradeops.ai.api.auth.application.port.in.SendPasswordResetEmailUseCase;
import cl.gradeops.ai.api.auth.application.port.in.SignOutUseCase;
import cl.gradeops.ai.api.auth.application.result.RegisterResult;
import cl.gradeops.ai.api.shared.infrastructure.config.security.AuthenticatedTeacher;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUseCase registerUseCase;
    private final SignOutUseCase signOutUseCase;
    private final SendPasswordResetEmailUseCase sendPasswordResetEmailUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterCommand command = RegisterCommand.builder()
                .idToken(request.idToken())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .build();
        RegisterResult result = registerUseCase.execute(command);
        return ResponseEntity.ok(new RegisterResponse(result.uid(), result.created()));
    }

    @PostMapping("/sign-out")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void signOut() {
        AuthenticatedTeacher teacher = (AuthenticatedTeacher)
            SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        signOutUseCase.execute(teacher.uid());
    }

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        SendPasswordResetEmailCommand command = SendPasswordResetEmailCommand.builder()
                .email(request.email())
                .build();
        sendPasswordResetEmailUseCase.execute(command);
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        ResetPasswordCommand command = ResetPasswordCommand.builder()
                .rawCode(request.code())
                .email(request.email())
                .password(request.password())
                .passwordRepeat(request.passwordRepeat())
                .build();
        resetPasswordUseCase.execute(command);
    }
}
