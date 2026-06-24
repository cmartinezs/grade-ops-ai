package cl.gradeops.ai.api.auth.application.orchestrator;

import cl.gradeops.ai.api.auth.application.command.IssuePasswordResetCodeCommand;
import cl.gradeops.ai.api.auth.application.command.SendPasswordResetEmailCommand;
import cl.gradeops.ai.api.auth.application.port.in.IssuePasswordResetCodeUseCase;
import cl.gradeops.ai.api.auth.application.port.out.EmailNotificationPort;
import cl.gradeops.ai.api.auth.application.result.IssuePasswordResetCodeResult;
import cl.gradeops.ai.api.domain.teacher.TeacherEntity;
import cl.gradeops.ai.api.domain.teacher.TeacherRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.Mockito.*;

class SendPasswordResetEmailOrchestratorTest {

    private final TeacherRepository teacherRepository = mock(TeacherRepository.class);
    private final IssuePasswordResetCodeUseCase issuePasswordResetCodeUseCase = mock(IssuePasswordResetCodeUseCase.class);
    private final EmailNotificationPort emailNotificationPort = mock(EmailNotificationPort.class);
    private final SendPasswordResetEmailOrchestrator orchestrator =
            new SendPasswordResetEmailOrchestrator(teacherRepository, issuePasswordResetCodeUseCase, emailNotificationPort, 30);

    @Test
    void unknown_email_no_interactions() {
        SendPasswordResetEmailCommand command = SendPasswordResetEmailCommand.builder().email("unknown@test.com").build();
        when(teacherRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        orchestrator.execute(command);

        verifyNoInteractions(issuePasswordResetCodeUseCase, emailNotificationPort);
    }

    @Test
    void google_provider_no_interactions() {
        TeacherEntity teacher = new TeacherEntity("uid-1", "Grace", "Hopper", "g@test.com", "GOOGLE");
        SendPasswordResetEmailCommand command = SendPasswordResetEmailCommand.builder().email("g@test.com").build();
        when(teacherRepository.findByEmail("g@test.com")).thenReturn(Optional.of(teacher));

        orchestrator.execute(command);

        verifyNoInteractions(issuePasswordResetCodeUseCase, emailNotificationPort);
    }

    @Test
    void email_password_teacher_triggers_code_and_email() {
        TeacherEntity teacher = new TeacherEntity("uid-2", "Ada", "Lovelace", "a@test.com", "EMAIL_PASSWORD");
        SendPasswordResetEmailCommand command = SendPasswordResetEmailCommand.builder().email("a@test.com").build();
        when(teacherRepository.findByEmail("a@test.com")).thenReturn(Optional.of(teacher));
        when(issuePasswordResetCodeUseCase.execute(any(IssuePasswordResetCodeCommand.class)))
                .thenReturn(IssuePasswordResetCodeResult.builder().rawCode("raw-123").expiresAt(Instant.now().plusSeconds(1800)).build());

        orchestrator.execute(command);

        verify(issuePasswordResetCodeUseCase).execute(argThat(c ->
                c.teacherUid().equals("uid-2") && c.ttlMinutes() == 30));
        verify(emailNotificationPort).sendPasswordResetEmail("a@test.com", "Ada", "raw-123");
    }
}
