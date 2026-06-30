package cl.gradeops.ai.api.auth.infrastructure.adapter.out.persistence;

import cl.gradeops.ai.api.auth.domain.model.PasswordResetCode;
import cl.gradeops.ai.api.auth.domain.valueobject.RawCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetCodePersistenceAdapterTest {

    @Mock PasswordResetCodeJpaRepository jpaRepository;
    @Mock PasswordResetCodePersistenceMapper mapper;
    @InjectMocks PasswordResetCodePersistenceAdapter adapter;

    @Test
    void shouldCreateEntityWhenCodeIsNew() {
        // given
        PasswordResetCode domain = PasswordResetCode.issue("uid-1", new RawCode("raw-code"),
                Instant.now().plus(1, ChronoUnit.HOURS));
        PasswordResetCodeJpaEntity entity = new PasswordResetCodeJpaEntity();
        entity.setTeacherUid("uid-1");
        entity.setRawCode("raw-code");
        when(jpaRepository.findByTeacherUid("uid-1")).thenReturn(Optional.empty());
        when(mapper.toEntity(domain)).thenReturn(entity);

        // when
        adapter.save(domain);

        // then
        verify(jpaRepository).findByTeacherUid("uid-1");
        verify(mapper).toEntity(domain);
        verify(mapper, never()).updateEntity(any(), any());
        verify(jpaRepository).save(entity);
    }

    @Test
    void shouldUpdateAllFieldsWhenCodeAlreadyExists() {
        // given
        Instant expiresAt = Instant.now().plus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.MILLIS);
        Instant createdAt = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        Instant usedAt = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        PasswordResetCode domain = PasswordResetCode.restore("uid-1", new RawCode("new-code"),
                expiresAt, createdAt, usedAt);

        PasswordResetCodeJpaEntity existing = new PasswordResetCodeJpaEntity();
        existing.setTeacherUid("uid-1");
        existing.setRawCode("old-code");
        when(jpaRepository.findByTeacherUid("uid-1")).thenReturn(Optional.of(existing));
        doAnswer(inv -> {
            PasswordResetCodeJpaEntity e = inv.getArgument(0);
            PasswordResetCode c = inv.getArgument(1);
            e.setRawCode(c.getRawCode().value());
            e.setExpiresAt(c.getExpiresAt());
            e.setCreatedAt(c.getCreatedAt());
            e.setUsedAt(c.getUsedAt());
            return null;
        }).when(mapper).updateEntity(any(), any());

        // when
        adapter.save(domain);

        // then
        verify(jpaRepository).findByTeacherUid("uid-1");
        verify(mapper).updateEntity(existing, domain);
        verify(mapper, never()).toEntity(any());
        ArgumentCaptor<PasswordResetCodeJpaEntity> captor = ArgumentCaptor.forClass(PasswordResetCodeJpaEntity.class);
        verify(jpaRepository).save(captor.capture());
        PasswordResetCodeJpaEntity saved = captor.getValue();
        assertThat(saved.getTeacherUid()).isEqualTo("uid-1");
        assertThat(saved.getRawCode()).isEqualTo("new-code");
        assertThat(saved.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(saved.getCreatedAt()).isEqualTo(createdAt);
        assertThat(saved.getUsedAt()).isEqualTo(usedAt);
    }

    @Test
    void shouldReturnDomainWhenCodeFoundByRawCode() {
        // given
        Instant expiresAt = Instant.now().plus(1, ChronoUnit.HOURS);
        Instant createdAt = Instant.now();
        PasswordResetCodeJpaEntity entity = new PasswordResetCodeJpaEntity();
        entity.setTeacherUid("uid-1");
        entity.setRawCode("raw-code");
        entity.setExpiresAt(expiresAt);
        entity.setCreatedAt(createdAt);
        when(jpaRepository.findByRawCode("raw-code")).thenReturn(Optional.of(entity));
        PasswordResetCode domain = PasswordResetCode.restore("uid-1", new RawCode("raw-code"),
                expiresAt, createdAt, null);
        when(mapper.toDomain(entity)).thenReturn(domain);

        // when
        Optional<PasswordResetCode> result = adapter.findByRawCode("raw-code");

        // then
        assertThat(result).contains(domain);
        verify(mapper).toDomain(entity);
    }

    @Test
    void shouldReturnEmptyWhenCodeNotFoundByRawCode() {
        // given
        when(jpaRepository.findByRawCode("missing")).thenReturn(Optional.empty());

        // when
        Optional<PasswordResetCode> result = adapter.findByRawCode("missing");

        // then
        assertThat(result).isEmpty();
        verify(mapper, never()).toDomain(any());
    }

    @Test
    void shouldDelegateToJpaRepositoryWhenDeletingByTeacherUid() {
        // when
        adapter.deleteByTeacherUid("uid-1");

        // then
        verify(jpaRepository).deleteByTeacherUid("uid-1");
    }

    @Test
    void shouldDelegateToBulkDeleteAndReturnLongCount() {
        // given
        Instant threshold = Instant.now().minus(90, java.time.temporal.ChronoUnit.DAYS);
        when(jpaRepository.bulkDeleteClosedCreatedBefore(threshold)).thenReturn(3);

        // when
        long result = adapter.deleteAllClosedCreatedBefore(threshold);

        // then
        verify(jpaRepository).bulkDeleteClosedCreatedBefore(threshold);
        assertThat(result).isEqualTo(3L);
    }

    @Test
    void shouldReturnZeroWhenNothingDeleted() {
        // given
        Instant threshold = Instant.now().minus(90, java.time.temporal.ChronoUnit.DAYS);
        when(jpaRepository.bulkDeleteClosedCreatedBefore(threshold)).thenReturn(0);

        // when
        long result = adapter.deleteAllClosedCreatedBefore(threshold);

        // then
        assertThat(result).isEqualTo(0L);
    }
}
