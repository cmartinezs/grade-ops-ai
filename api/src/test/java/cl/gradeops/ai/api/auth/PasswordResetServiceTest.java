package cl.gradeops.ai.api.auth;

import cl.gradeops.ai.api.shared.infrastructure.config.GradeOpsEmailProperties;
import cl.gradeops.ai.api.shared.infrastructure.config.GradeOpsWebProperties;
import cl.gradeops.ai.api.domain.teacher.TeacherEntity;
import cl.gradeops.ai.api.domain.teacher.TeacherRepository;
import cl.gradeops.ai.api.shared.infrastructure.adapter.out.email.JavaMailEmailService;
import cl.gradeops.ai.api.port.AuthPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock TeacherRepository teacherRepository;
    @Mock PasswordResetCodeRepository codeRepository;
    @Mock JavaMailEmailService emailService;
    @Mock AuthPort authPort;

    PasswordResetService service;

    @BeforeEach
    void setUp() {
        GradeOpsEmailProperties emailProps = new GradeOpsEmailProperties();
        GradeOpsWebProperties webProps = new GradeOpsWebProperties();
        webProps.setBaseUrl("http://localhost:3000");

        service = new PasswordResetService(
            teacherRepository, codeRepository, emailService, authPort, emailProps, webProps);
    }

    // ─── sendResetEmail ────────────────────────────────────────────────────────

    @Test
    void sendResetEmail_unknownEmail_doesNothing() {
        when(teacherRepository.findByEmail("unknown@school.cl")).thenReturn(Optional.empty());

        service.sendResetEmail(new ForgotPasswordRequest("unknown@school.cl"));

        verifyNoInteractions(codeRepository, emailService);
    }

    @Test
    void sendResetEmail_googleProvider_doesNothing() {
        TeacherEntity googleTeacher = new TeacherEntity("uid-g", "Ana", "López", "ana@gmail.com", "GOOGLE");
        when(teacherRepository.findByEmail("ana@gmail.com")).thenReturn(Optional.of(googleTeacher));

        service.sendResetEmail(new ForgotPasswordRequest("ana@gmail.com"));

        verifyNoInteractions(codeRepository, emailService);
    }

    @Test
    void sendResetEmail_emailPasswordTeacher_savesCodeAndSendsEmail() {
        TeacherEntity teacher = new TeacherEntity("uid-ep", "Pedro", "Soto", "pedro@school.cl", "EMAIL_PASSWORD");
        when(teacherRepository.findByEmail("pedro@school.cl")).thenReturn(Optional.of(teacher));

        service.sendResetEmail(new ForgotPasswordRequest("pedro@school.cl"));

        verify(codeRepository).deleteByTeacherUid("uid-ep");

        ArgumentCaptor<PasswordResetCodeEntity> codeCaptor =
            ArgumentCaptor.forClass(PasswordResetCodeEntity.class);
        verify(codeRepository).save(codeCaptor.capture());

        PasswordResetCodeEntity saved = codeCaptor.getValue();
        assertThat(saved.getTeacherUid()).isEqualTo("uid-ep");
        assertThat(saved.getCode()).hasSize(36); // UUID with hyphens
        assertThat(saved.getExpiresAt()).isAfter(Instant.now());

        ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendPasswordReset(
            eq("pedro@school.cl"), eq("Pedro"), linkCaptor.capture());
        assertThat(linkCaptor.getValue())
            .startsWith("http://localhost:3000/reset-password?code=");
    }

    @Test
    void sendResetEmail_deletesExistingCodeBeforeCreatingNew() {
        TeacherEntity teacher = new TeacherEntity("uid-ep", "Pedro", "Soto", "pedro@school.cl", "EMAIL_PASSWORD");
        when(teacherRepository.findByEmail("pedro@school.cl")).thenReturn(Optional.of(teacher));

        service.sendResetEmail(new ForgotPasswordRequest("pedro@school.cl"));
        service.sendResetEmail(new ForgotPasswordRequest("pedro@school.cl"));

        verify(codeRepository, times(2)).deleteByTeacherUid("uid-ep");
        verify(codeRepository, times(2)).save(any(PasswordResetCodeEntity.class));
    }

    // ─── resetPassword ─────────────────────────────────────────────────────────

    @Test
    void resetPassword_codeNotFound_throws404() {
        when(codeRepository.findByCode("bad-code")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            service.resetPassword("bad-code", new ResetPasswordRequest("x@x.cl", "pass12", "pass12")))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void resetPassword_codeExpired_throws410WithExpiredReason() {
        PasswordResetCodeEntity expired = new PasswordResetCodeEntity(
            "uid-ep", "code-xyz", Instant.now().minus(1, ChronoUnit.HOURS));
        when(codeRepository.findByCode("code-xyz")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() ->
            service.resetPassword("code-xyz", new ResetPasswordRequest("x@x.cl", "pass12", "pass12")))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(e -> {
                ResponseStatusException rse = (ResponseStatusException) e;
                assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.GONE);
                assertThat(rse.getReason()).isEqualTo("RESET_CODE_EXPIRED");
            });
    }

    @Test
    void resetPassword_codeAlreadyUsed_throws410WithUsedReason() {
        PasswordResetCodeEntity used = new PasswordResetCodeEntity(
            "uid-ep", "code-xyz", Instant.now().plus(30, ChronoUnit.MINUTES));
        used.markUsed();
        when(codeRepository.findByCode("code-xyz")).thenReturn(Optional.of(used));

        assertThatThrownBy(() ->
            service.resetPassword("code-xyz", new ResetPasswordRequest("x@x.cl", "pass12", "pass12")))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(e -> {
                ResponseStatusException rse = (ResponseStatusException) e;
                assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.GONE);
                assertThat(rse.getReason()).isEqualTo("RESET_CODE_USED");
            });
    }

    @Test
    void resetPassword_emailMismatch_throws422() {
        PasswordResetCodeEntity code = new PasswordResetCodeEntity(
            "uid-ep", "code-xyz", Instant.now().plus(30, ChronoUnit.MINUTES));
        when(codeRepository.findByCode("code-xyz")).thenReturn(Optional.of(code));

        TeacherEntity teacher = new TeacherEntity("uid-ep", "Pedro", "Soto", "pedro@school.cl", "EMAIL_PASSWORD");
        when(teacherRepository.findById("uid-ep")).thenReturn(Optional.of(teacher));

        assertThatThrownBy(() ->
            service.resetPassword("code-xyz",
                new ResetPasswordRequest("wrong@school.cl", "pass12", "pass12")))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(e -> {
                ResponseStatusException rse = (ResponseStatusException) e;
                assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
                assertThat(rse.getReason()).isEqualTo("RESET_CODE_EMAIL_MISMATCH");
            });
    }

    @Test
    void resetPassword_valid_updatesPasswordAndMarksCodeUsed() {
        PasswordResetCodeEntity code = new PasswordResetCodeEntity(
            "uid-ep", "code-xyz", Instant.now().plus(30, ChronoUnit.MINUTES));
        when(codeRepository.findByCode("code-xyz")).thenReturn(Optional.of(code));

        TeacherEntity teacher = new TeacherEntity("uid-ep", "Pedro", "Soto", "pedro@school.cl", "EMAIL_PASSWORD");
        when(teacherRepository.findById("uid-ep")).thenReturn(Optional.of(teacher));

        service.resetPassword("code-xyz",
            new ResetPasswordRequest("pedro@school.cl", "nuevaPass99", "nuevaPass99"));

        verify(authPort).updatePassword("uid-ep", "nuevaPass99");
        verify(authPort).revokeRefreshTokens("uid-ep");
        assertThat(code.isUsed()).isTrue();
        verify(codeRepository).save(code);
    }

    @Test
    void resetPassword_passwordMismatch_throws422() {
        assertThatThrownBy(() ->
            service.resetPassword("code-xyz",
                new ResetPasswordRequest("pedro@school.cl", "pass123", "different")))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(e -> {
                ResponseStatusException rse = (ResponseStatusException) e;
                assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
                assertThat(rse.getReason()).isEqualTo("PASSWORD_MISMATCH");
            });
    }
}
