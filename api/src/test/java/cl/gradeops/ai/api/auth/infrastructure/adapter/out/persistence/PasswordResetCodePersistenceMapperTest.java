package cl.gradeops.ai.api.auth.infrastructure.adapter.out.persistence;

import cl.gradeops.ai.api.auth.domain.model.PasswordResetCode;
import cl.gradeops.ai.api.auth.domain.valueobject.RawCode;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordResetCodePersistenceMapperTest {

    private final PasswordResetCodePersistenceMapper mapper = new PasswordResetCodePersistenceMapper();

    @Test
    void shouldMapAllFieldsAndLeaveIdNullWhenMappingToEntity() {
        // given
        Instant expiresAt = Instant.now().plus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.MILLIS);
        Instant createdAt = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        PasswordResetCode domain = PasswordResetCode.restore("uid-1", new RawCode("raw-code"),
                expiresAt, createdAt, null);

        // when
        PasswordResetCodeJpaEntity entity = mapper.toEntity(domain);

        // then
        assertThat(entity.getId()).isNull();
        assertThat(entity.getTeacherUid()).isEqualTo("uid-1");
        assertThat(entity.getRawCode()).isEqualTo("raw-code");
        assertThat(entity.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
        assertThat(entity.getUsedAt()).isNull();
    }

    @Test
    void shouldMapUsedAtWhenMappingUsedCodeToEntity() {
        // given
        Instant usedAt = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        PasswordResetCode domain = PasswordResetCode.restore("uid-1", new RawCode("raw-code"),
                Instant.now().plus(1, ChronoUnit.HOURS), Instant.now(), usedAt);

        // when
        PasswordResetCodeJpaEntity entity = mapper.toEntity(domain);

        // then
        assertThat(entity.getUsedAt()).isEqualTo(usedAt);
    }

    @Test
    void shouldUpdateAllPersistableFieldsButPreserveBusinessKeyWhenUpdatingEntity() {
        // given
        Instant newExpiresAt = Instant.now().plus(2, ChronoUnit.HOURS).truncatedTo(ChronoUnit.MILLIS);
        Instant newCreatedAt = Instant.now().minus(5, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MILLIS);
        Instant newUsedAt = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        PasswordResetCode updated = PasswordResetCode.restore("uid-1", new RawCode("new-code"),
                newExpiresAt, newCreatedAt, newUsedAt);

        PasswordResetCodeJpaEntity entity = new PasswordResetCodeJpaEntity();
        entity.setTeacherUid("uid-1");
        entity.setRawCode("old-code");
        entity.setExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));
        entity.setCreatedAt(Instant.now().minus(10, ChronoUnit.MINUTES));

        // when
        mapper.updateEntity(entity, updated);

        // then
        assertThat(entity.getTeacherUid()).isEqualTo("uid-1");
        assertThat(entity.getRawCode()).isEqualTo("new-code");
        assertThat(entity.getExpiresAt()).isEqualTo(newExpiresAt);
        assertThat(entity.getCreatedAt()).isEqualTo(newCreatedAt);
        assertThat(entity.getUsedAt()).isEqualTo(newUsedAt);
        assertThat(entity.getId()).isNull();
    }

    @Test
    void shouldMapAllFieldsWhenMappingToDomain() {
        // given
        Instant expiresAt = Instant.now().plus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.MILLIS);
        Instant createdAt = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        Instant usedAt = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        PasswordResetCodeJpaEntity entity = new PasswordResetCodeJpaEntity();
        entity.setTeacherUid("uid-1");
        entity.setRawCode("raw-code");
        entity.setExpiresAt(expiresAt);
        entity.setCreatedAt(createdAt);
        entity.setUsedAt(usedAt);

        // when
        PasswordResetCode domain = mapper.toDomain(entity);

        // then
        assertThat(domain.getTeacherUid()).isEqualTo("uid-1");
        assertThat(domain.getRawCode().value()).isEqualTo("raw-code");
        assertThat(domain.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(domain.getCreatedAt()).isEqualTo(createdAt);
        assertThat(domain.getUsedAt()).isEqualTo(usedAt);
    }
}
