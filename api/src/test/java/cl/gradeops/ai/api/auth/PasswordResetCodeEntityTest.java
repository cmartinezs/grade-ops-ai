package cl.gradeops.ai.api.auth;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordResetCodeEntityTest {

    @Test
    void isExpired_returnsFalse_whenExpiresAtIsInFuture() {
        var entity = new PasswordResetCodeEntity("uid-1", "code-1",
            Instant.now().plus(30, ChronoUnit.MINUTES));
        assertThat(entity.isExpired()).isFalse();
    }

    @Test
    void isExpired_returnsTrue_whenExpiresAtIsInPast() {
        var entity = new PasswordResetCodeEntity("uid-1", "code-1",
            Instant.now().minus(1, ChronoUnit.HOURS));
        assertThat(entity.isExpired()).isTrue();
    }

    @Test
    void isUsed_returnsFalse_beforeMarkUsed() {
        var entity = new PasswordResetCodeEntity("uid-1", "code-1",
            Instant.now().plus(30, ChronoUnit.MINUTES));
        assertThat(entity.isUsed()).isFalse();
    }

    @Test
    void isUsed_returnsTrue_afterMarkUsed() {
        var entity = new PasswordResetCodeEntity("uid-1", "code-1",
            Instant.now().plus(30, ChronoUnit.MINUTES));
        entity.markUsed();
        assertThat(entity.isUsed()).isTrue();
    }

    @Test
    void markUsed_setsUsedAtToNonNull() {
        var entity = new PasswordResetCodeEntity("uid-1", "code-1",
            Instant.now().plus(30, ChronoUnit.MINUTES));
        assertThat(entity.getUsedAt()).isNull();
        entity.markUsed();
        assertThat(entity.getUsedAt()).isNotNull();
    }
}
