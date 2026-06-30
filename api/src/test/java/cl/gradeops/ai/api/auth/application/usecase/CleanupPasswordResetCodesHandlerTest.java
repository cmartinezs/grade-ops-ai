package cl.gradeops.ai.api.auth.application.usecase;

import cl.gradeops.ai.api.auth.application.port.out.PasswordResetCodeRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CleanupPasswordResetCodesHandlerTest {

    @Mock
    PasswordResetCodeRepositoryPort codeRepository;

    @Test
    void shouldCalculateThresholdFromFixedClockAndDelegate() {
        // given
        Clock fixedClock = Clock.fixed(Instant.parse("2026-06-29T02:00:00Z"), ZoneOffset.UTC);
        Instant expectedThreshold = Instant.parse("2026-03-31T02:00:00Z");
        CleanupPasswordResetCodesHandler handler = new CleanupPasswordResetCodesHandler(codeRepository, fixedClock, 90);
        when(codeRepository.deleteAllClosedCreatedBefore(expectedThreshold)).thenReturn(5L);

        // when
        long result = handler.execute();

        // then
        verify(codeRepository).deleteAllClosedCreatedBefore(expectedThreshold);
        assertThat(result).isEqualTo(5L);
    }

    @Test
    void shouldReturnCountFromRepository() {
        // given
        Clock fixedClock = Clock.fixed(Instant.parse("2026-06-29T02:00:00Z"), ZoneOffset.UTC);
        Instant expectedThreshold = Instant.parse("2026-03-31T02:00:00Z");
        CleanupPasswordResetCodesHandler handler = new CleanupPasswordResetCodesHandler(codeRepository, fixedClock, 90);
        when(codeRepository.deleteAllClosedCreatedBefore(expectedThreshold)).thenReturn(0L);

        // when
        long result = handler.execute();

        // then
        assertThat(result).isEqualTo(0L);
    }
}
