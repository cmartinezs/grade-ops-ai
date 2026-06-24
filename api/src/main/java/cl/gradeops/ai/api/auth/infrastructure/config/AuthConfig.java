package cl.gradeops.ai.api.auth.infrastructure.config;

import cl.gradeops.ai.api.auth.application.orchestrator.ResetPasswordOrchestrator;
import cl.gradeops.ai.api.auth.application.orchestrator.SendPasswordResetEmailOrchestrator;
import cl.gradeops.ai.api.auth.application.port.out.AuthPort;
import cl.gradeops.ai.api.auth.application.port.out.EmailNotificationPort;
import cl.gradeops.ai.api.auth.application.port.out.PasswordResetCodeRepositoryPort;
import cl.gradeops.ai.api.auth.application.usecase.IssuePasswordResetCodeHandler;
import cl.gradeops.ai.api.auth.application.usecase.RegisterHandler;
import cl.gradeops.ai.api.auth.application.usecase.RevokeRefreshTokensHandler;
import cl.gradeops.ai.api.auth.application.usecase.SignOutHandler;
import cl.gradeops.ai.api.auth.infrastructure.adapter.out.email.ThymeleafEmailNotificationAdapter;
import cl.gradeops.ai.api.auth.infrastructure.adapter.out.firebase.FirebaseAuthAdapter;
import cl.gradeops.ai.api.auth.infrastructure.adapter.out.persistence.PasswordResetCodeJpaRepository;
import cl.gradeops.ai.api.auth.infrastructure.adapter.out.persistence.PasswordResetCodePersistenceAdapter;
import cl.gradeops.ai.api.auth.infrastructure.adapter.out.persistence.PasswordResetCodePersistenceMapper;
import cl.gradeops.ai.api.domain.teacher.TeacherRepository;
import cl.gradeops.ai.api.shared.infrastructure.adapter.out.email.JavaMailEmailService;
import cl.gradeops.ai.api.shared.infrastructure.config.GradeOpsEmailProperties;
import com.google.firebase.auth.FirebaseAuth;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
class AuthConfig {

    private final GradeOpsEmailProperties emailProperties;

    @Bean
    FirebaseAuthAdapter firebaseAuthAdapter(FirebaseAuth firebaseAuth) {
        return new FirebaseAuthAdapter(firebaseAuth);
    }

    @Bean
    PasswordResetCodePersistenceMapper passwordResetCodePersistenceMapper() {
        return new PasswordResetCodePersistenceMapper();
    }

    @Bean
    PasswordResetCodePersistenceAdapter passwordResetCodePersistenceAdapter(
            PasswordResetCodeJpaRepository jpaRepository,
            PasswordResetCodePersistenceMapper mapper) {
        return new PasswordResetCodePersistenceAdapter(jpaRepository, mapper);
    }

    @Bean
    ThymeleafEmailNotificationAdapter thymeleafEmailNotificationAdapter(
            JavaMailEmailService javaMailEmailService,
            @Value("${gradeops.web.base-url}") String webBaseUrl) {
        return new ThymeleafEmailNotificationAdapter(javaMailEmailService, webBaseUrl);
    }

    @Bean
    RevokeRefreshTokensHandler revokeRefreshTokensHandler(AuthPort authPort) {
        return new RevokeRefreshTokensHandler(authPort);
    }

    @Bean
    SignOutHandler signOutHandler(cl.gradeops.ai.api.auth.application.port.in.RevokeRefreshTokensUseCase revokeRefreshTokensUseCase) {
        return new SignOutHandler(revokeRefreshTokensUseCase);
    }

    @Bean
    RegisterHandler registerHandler(AuthPort authPort, TeacherRepository teacherRepository) {
        return new RegisterHandler(authPort, teacherRepository);
    }

    @Bean
    IssuePasswordResetCodeHandler issuePasswordResetCodeHandler(
            PasswordResetCodeRepositoryPort codeRepository) {
        return new IssuePasswordResetCodeHandler(codeRepository);
    }

    @Bean
    SendPasswordResetEmailOrchestrator sendPasswordResetEmailOrchestrator(
            TeacherRepository teacherRepository,
            cl.gradeops.ai.api.auth.application.port.in.IssuePasswordResetCodeUseCase issuePasswordResetCodeUseCase,
            EmailNotificationPort emailNotificationPort) {
        int ttlMinutes = emailProperties.getResetPassword().getTtlMinutes();
        return new SendPasswordResetEmailOrchestrator(teacherRepository, issuePasswordResetCodeUseCase, emailNotificationPort, ttlMinutes);
    }

    @Bean
    ResetPasswordOrchestrator resetPasswordOrchestrator(
            PasswordResetCodeRepositoryPort codeRepository,
            AuthPort authPort,
            cl.gradeops.ai.api.auth.application.port.in.RevokeRefreshTokensUseCase revokeRefreshTokensUseCase,
            TeacherRepository teacherRepository) {
        return new ResetPasswordOrchestrator(codeRepository, authPort, revokeRefreshTokensUseCase, teacherRepository);
    }
}
