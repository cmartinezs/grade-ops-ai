package cl.gradeops.ai.api.auth.infrastructure.adapter.in.web;

import cl.gradeops.ai.api.auth.application.command.RegisterCommand;
import cl.gradeops.ai.api.auth.application.command.ResetPasswordCommand;
import cl.gradeops.ai.api.auth.application.command.SendPasswordResetEmailCommand;
import cl.gradeops.ai.api.auth.application.port.in.RegisterUseCase;
import cl.gradeops.ai.api.auth.application.port.in.ResetPasswordUseCase;
import cl.gradeops.ai.api.auth.application.port.in.SendPasswordResetEmailUseCase;
import cl.gradeops.ai.api.auth.application.port.in.SignOutUseCase;
import cl.gradeops.ai.api.auth.application.port.out.AuthPort;
import cl.gradeops.ai.api.auth.application.result.RegisterResult;
import cl.gradeops.ai.api.auth.domain.exception.InvalidResetCodeException;
import cl.gradeops.ai.api.config.FirebaseTestConfig;
import cl.gradeops.ai.api.domain.teacher.TeacherRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(FirebaseTestConfig.class)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean RegisterUseCase registerUseCase;
    @MockitoBean SignOutUseCase signOutUseCase;
    @MockitoBean SendPasswordResetEmailUseCase sendPasswordResetEmailUseCase;
    @MockitoBean ResetPasswordUseCase resetPasswordUseCase;
    @MockitoBean AuthPort authPort;
    @MockitoBean TeacherRepository teacherRepository;

    // ─── POST /register ──────────────────────────────────────────────────────

    @Test
    void register_existingTeacher_returns200CreatedFalse() throws Exception {
        when(registerUseCase.execute(any(RegisterCommand.class)))
                .thenReturn(new RegisterResult("uid-1", false));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idToken": "valid-token", "firstName": "Grace", "lastName": "Hopper"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uid").value("uid-1"))
                .andExpect(jsonPath("$.created").value(false));
    }

    @Test
    void register_newTeacher_returns200CreatedTrue() throws Exception {
        when(registerUseCase.execute(any(RegisterCommand.class)))
                .thenReturn(new RegisterResult("uid-2", true));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idToken": "valid-token", "firstName": "Ada", "lastName": "Lovelace"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uid").value("uid-2"))
                .andExpect(jsonPath("$.created").value(true));
    }

    // ─── POST /sign-out ──────────────────────────────────────────────────────

    @Test
    void signOut_authenticatedUser_returns204() throws Exception {
        cl.gradeops.ai.api.auth.domain.model.TeacherIdentity identity =
                new cl.gradeops.ai.api.auth.domain.model.TeacherIdentity("uid-1", "t@school.com", true, "Teacher", "EMAIL_PASSWORD");
        when(authPort.verifyToken("valid-token")).thenReturn(identity);

        mockMvc.perform(post("/api/v1/auth/sign-out")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNoContent());

        verify(signOutUseCase).execute("uid-1");
    }

    // ─── POST /forgot-password ───────────────────────────────────────────────

    @Test
    void forgotPassword_validEmail_returns204() throws Exception {
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "teacher@school.cl"}
                                """))
                .andExpect(status().isNoContent());

        verify(sendPasswordResetEmailUseCase).execute(any(SendPasswordResetEmailCommand.class));
    }

    // ─── POST /reset-password ────────────────────────────────────────────────

    @Test
    void resetPassword_validCode_returns204() throws Exception {
        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code": "valid-code", "email": "teacher@school.cl", "password": "newPass99", "passwordRepeat": "newPass99"}
                                """))
                .andExpect(status().isNoContent());

        verify(resetPasswordUseCase).execute(any(ResetPasswordCommand.class));
    }

    @Test
    void resetPassword_invalidCode_returns422() throws Exception {
        doThrow(new InvalidResetCodeException("code not found"))
                .when(resetPasswordUseCase).execute(any(ResetPasswordCommand.class));

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code": "bad-code", "email": "teacher@school.cl", "password": "newPass99", "passwordRepeat": "newPass99"}
                                """))
                .andExpect(status().isUnprocessableEntity());
    }
}
