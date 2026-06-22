package cl.gradeops.ai.api.auth;

import cl.gradeops.ai.api.config.FirebaseTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(FirebaseTestConfig.class)
class PasswordResetControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean PasswordResetService passwordResetService;

    // ─── POST /forgot-password ──────────────────────────────────────────────────

    @Test
    void forgotPassword_validEmail_returns200() throws Exception {
        doNothing().when(passwordResetService).sendResetEmail(any());

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email": "teacher@school.cl"}
                    """))
            .andExpect(status().isOk());
    }

    @Test
    void forgotPassword_blankEmail_returns422() throws Exception {
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email": ""}
                    """))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void forgotPassword_invalidEmailFormat_returns422() throws Exception {
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email": "not-an-email"}
                    """))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    // ─── PUT /reset-password ───────────────────────────────────────────────────

    @Test
    void resetPassword_validRequest_returns200() throws Exception {
        doNothing().when(passwordResetService).resetPassword(eq("valid-code"), any());

        mockMvc.perform(put("/api/v1/auth/reset-password")
                .param("code", "valid-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"teacher@school.cl","password":"newPass99","passwordRepeat":"newPass99"}
                    """))
            .andExpect(status().isOk());
    }

    @Test
    void resetPassword_codeNotFound_returns404() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "RESET_CODE_NOT_FOUND"))
            .when(passwordResetService).resetPassword(eq("bad-code"), any());

        mockMvc.perform(put("/api/v1/auth/reset-password")
                .param("code", "bad-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"teacher@school.cl","password":"newPass99","passwordRepeat":"newPass99"}
                    """))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("RESET_CODE_NOT_FOUND"));
    }

    @Test
    void resetPassword_codeExpired_returns410WithExpiredCode() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.GONE, "RESET_CODE_EXPIRED"))
            .when(passwordResetService).resetPassword(eq("expired-code"), any());

        mockMvc.perform(put("/api/v1/auth/reset-password")
                .param("code", "expired-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"teacher@school.cl","password":"newPass99","passwordRepeat":"newPass99"}
                    """))
            .andExpect(status().isGone())
            .andExpect(jsonPath("$.error").value("RESET_CODE_EXPIRED"));
    }

    @Test
    void resetPassword_codeUsed_returns410WithUsedCode() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.GONE, "RESET_CODE_USED"))
            .when(passwordResetService).resetPassword(eq("used-code"), any());

        mockMvc.perform(put("/api/v1/auth/reset-password")
                .param("code", "used-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"teacher@school.cl","password":"newPass99","passwordRepeat":"newPass99"}
                    """))
            .andExpect(status().isGone())
            .andExpect(jsonPath("$.error").value("RESET_CODE_USED"));
    }

    @Test
    void resetPassword_emailMismatch_returns422() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "RESET_CODE_EMAIL_MISMATCH"))
            .when(passwordResetService).resetPassword(eq("ok-code"), any());

        mockMvc.perform(put("/api/v1/auth/reset-password")
                .param("code", "ok-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"wrong@school.cl","password":"newPass99","passwordRepeat":"newPass99"}
                    """))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.error").value("RESET_CODE_EMAIL_MISMATCH"));
    }

    @Test
    void resetPassword_shortPassword_returns422() throws Exception {
        mockMvc.perform(put("/api/v1/auth/reset-password")
                .param("code", "ok-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"teacher@school.cl","password":"abc","passwordRepeat":"abc"}
                    """))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void resetPassword_noCodeParam_returns400() throws Exception {
        mockMvc.perform(put("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"teacher@school.cl","password":"newPass99","passwordRepeat":"newPass99"}
                    """))
            .andExpect(status().isBadRequest());
    }
}
