package cl.gradeops.ai.api.auth.infrastructure.adapter.out.persistence;

import cl.gradeops.ai.api.auth.domain.model.PasswordResetCode;
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
    void save_mapsDomainToEntity_thenPersists() {
        PasswordResetCode domain = PasswordResetCode.issue("uid-1", "raw-code", Instant.now().plus(1, ChronoUnit.HOURS));
        PasswordResetCodeJpaEntity entity = new PasswordResetCodeJpaEntity();
        entity.setTeacherUid("uid-1");
        entity.setRawCode("raw-code");
        when(mapper.toEntity(domain)).thenReturn(entity);

        adapter.save(domain);

        verify(mapper).toEntity(domain);
        verify(jpaRepository).save(entity);
    }

    @Test
    void findByRawCode_found_returnsDomain() {
        PasswordResetCodeJpaEntity entity = new PasswordResetCodeJpaEntity();
        entity.setTeacherUid("uid-1");
        entity.setRawCode("raw-code");
        entity.setExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));
        entity.setCreatedAt(Instant.now());
        when(jpaRepository.findByRawCode("raw-code")).thenReturn(Optional.of(entity));

        PasswordResetCode domain = PasswordResetCode.restore("uid-1", "raw-code",
            entity.getExpiresAt(), entity.getCreatedAt(), null);
        when(mapper.toDomain(entity)).thenReturn(domain);

        Optional<PasswordResetCode> result = adapter.findByRawCode("raw-code");

        assertThat(result).contains(domain);
        verify(mapper).toDomain(entity);
    }

    @Test
    void findByRawCode_notFound_returnsEmpty() {
        when(jpaRepository.findByRawCode("missing")).thenReturn(Optional.empty());

        Optional<PasswordResetCode> result = adapter.findByRawCode("missing");

        assertThat(result).isEmpty();
        verify(mapper, never()).toDomain(any());
    }

    @Test
    void deleteByTeacherUid_delegatesToJpaRepo() {
        adapter.deleteByTeacherUid("uid-1");

        verify(jpaRepository).deleteByTeacherUid("uid-1");
    }
}
