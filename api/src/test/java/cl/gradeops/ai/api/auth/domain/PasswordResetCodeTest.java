package cl.gradeops.ai.api.auth.domain;

import cl.gradeops.ai.api.auth.domain.model.PasswordResetCode;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordResetCodeTest {

    @Test
    void issue_creates_non_expired_non_used_code() {
        Instant future = Instant.now().plus(30, ChronoUnit.MINUTES);
        PasswordResetCode code = PasswordResetCode.issue("uid-1", "raw-abc", future);

        assertThat(code.isUsed()).isFalse();
        assertThat(code.isExpired()).isFalse();
        assertThat(code.getTeacherUid()).isEqualTo("uid-1");
        assertThat(code.getRawCode()).isEqualTo("raw-abc");
        assertThat(code.getCreatedAt()).isNotNull();
        assertThat(code.getUsedAt()).isNull();
    }

    @Test
    void isExpired_returns_true_for_past_expiry() {
        Instant past = Instant.now().minus(1, ChronoUnit.HOURS);
        PasswordResetCode code = PasswordResetCode.issue("uid-1", "raw-abc", past);

        assertThat(code.isExpired()).isTrue();
    }

    @Test
    void markUsed_sets_isUsed_to_true() {
        Instant future = Instant.now().plus(30, ChronoUnit.MINUTES);
        PasswordResetCode code = PasswordResetCode.issue("uid-1", "raw-abc", future);

        code.markUsed();

        assertThat(code.isUsed()).isTrue();
        assertThat(code.getUsedAt()).isNotNull();
    }

    @Test
    void restore_reconstructs_used_code() {
        Instant future = Instant.now().plus(30, ChronoUnit.MINUTES);
        Instant past = Instant.now().minus(1, ChronoUnit.HOURS);
        PasswordResetCode code = PasswordResetCode.restore("uid-1", "raw-abc", future, past, past);

        assertThat(code.isUsed()).isTrue();
        assertThat(code.getTeacherUid()).isEqualTo("uid-1");
    }

    @Test
    void restore_reconstructs_unused_code() {
        Instant future = Instant.now().plus(30, ChronoUnit.MINUTES);
        Instant past = Instant.now().minus(1, ChronoUnit.HOURS);
        PasswordResetCode code = PasswordResetCode.restore("uid-1", "raw-abc", future, past, null);

        assertThat(code.isUsed()).isFalse();
    }
}
