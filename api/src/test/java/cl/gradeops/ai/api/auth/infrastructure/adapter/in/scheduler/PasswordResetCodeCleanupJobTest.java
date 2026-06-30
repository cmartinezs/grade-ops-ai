package cl.gradeops.ai.api.auth.infrastructure.adapter.in.scheduler;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import cl.gradeops.ai.api.auth.application.port.in.CleanupPasswordResetCodesUseCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetCodeCleanupJobTest {

    @Mock
    CleanupPasswordResetCodesUseCase cleanupUseCase;

    PasswordResetCodeCleanupJob job;
    ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        job = new PasswordResetCodeCleanupJob(cleanupUseCase);
        Logger logger = (Logger) LoggerFactory.getLogger(PasswordResetCodeCleanupJob.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        Logger logger = (Logger) LoggerFactory.getLogger(PasswordResetCodeCleanupJob.class);
        logger.detachAppender(listAppender);
    }

    @Test
    void shouldDelegateToUseCaseAndLogSuccessFields() {
        // given
        when(cleanupUseCase.getRetentionDays()).thenReturn(90);
        when(cleanupUseCase.execute()).thenReturn(7L);

        // when
        job.run();

        // then
        verify(cleanupUseCase).getRetentionDays();
        verify(cleanupUseCase).execute();
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anySatisfy(e -> {
            assertThat(e.getLevel()).isEqualTo(Level.INFO);
            assertThat(e.getFormattedMessage()).contains("retentionDays=90");
        });
        assertThat(logs).anySatisfy(e -> {
            assertThat(e.getLevel()).isEqualTo(Level.INFO);
            assertThat(e.getFormattedMessage()).contains("deleted=7");
            assertThat(e.getFormattedMessage()).contains("durationMs=");
        });
    }

    @Test
    void shouldNotPropagateExceptionAndLogError() {
        // given
        when(cleanupUseCase.execute()).thenThrow(new RuntimeException("DB unavailable"));

        // when / then
        assertThatCode(() -> job.run()).doesNotThrowAnyException();
        assertThat(listAppender.list).anySatisfy(e -> {
            assertThat(e.getLevel()).isEqualTo(Level.ERROR);
            assertThat(e.getFormattedMessage()).contains("cleanup failed");
        });
    }
}
