package cl.gradeops.ai.api.auth.domain;

import cl.gradeops.ai.api.auth.domain.model.PasswordResetCode;
import cl.gradeops.ai.api.auth.domain.valueobject.RawCode;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordResetCodeTest {

    @Test
    void shouldCreateNonExpiredNonUsedCodeWhenIssued() {
        Instant future = Instant.now().plus(30, ChronoUnit.MINUTES);
        PasswordResetCode code = PasswordResetCode.issue("uid-1", new RawCode("raw-abc"), future);

        assertThat(code.isUsed()).isFalse();
        assertThat(code.isExpired()).isFalse();
        assertThat(code.getTeacherUid()).isEqualTo("uid-1");
        assertThat(code.getRawCode().value()).isEqualTo("raw-abc");
        assertThat(code.getCreatedAt()).isNotNull();
        assertThat(code.getUsedAt()).isNull();
    }

    @Test
    void shouldReturnTrueForIsExpiredWhenExpiryIsPast() {
        Instant past = Instant.now().minus(1, ChronoUnit.HOURS);
        PasswordResetCode code = PasswordResetCode.issue("uid-1", new RawCode("raw-abc"), past);

        assertThat(code.isExpired()).isTrue();
    }

    @Test
    void shouldSetIsUsedToTrueWhenMarkUsedCalled() {
        Instant future = Instant.now().plus(30, ChronoUnit.MINUTES);
        PasswordResetCode code = PasswordResetCode.issue("uid-1", new RawCode("raw-abc"), future);

        code.markUsed();

        assertThat(code.isUsed()).isTrue();
        assertThat(code.getUsedAt()).isNotNull();
    }

    @Test
    void shouldReconstructUsedCodeWhenRestored() {
        Instant future = Instant.now().plus(30, ChronoUnit.MINUTES);
        Instant past = Instant.now().minus(1, ChronoUnit.HOURS);
        PasswordResetCode code = PasswordResetCode.restore("uid-1", new RawCode("raw-abc"), future, past, past);

        assertThat(code.isUsed()).isTrue();
        assertThat(code.getTeacherUid()).isEqualTo("uid-1");
    }

    @Test
    void shouldReconstructUnusedCodeWhenRestored() {
        Instant future = Instant.now().plus(30, ChronoUnit.MINUTES);
        Instant past = Instant.now().minus(1, ChronoUnit.HOURS);
        PasswordResetCode code = PasswordResetCode.restore("uid-1", new RawCode("raw-abc"), future, past, null);

        assertThat(code.isUsed()).isFalse();
    }
}
