package cl.gradeops.ai.api.teacher.infrastructure.adapter.out.persistence;

import cl.gradeops.ai.api.teacher.domain.model.AuthProvider;
import cl.gradeops.ai.api.teacher.domain.model.Teacher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeacherPersistenceAdapterTest {

    @Mock TeacherJpaRepository jpaRepository;
    TeacherPersistenceMapper mapper = new TeacherPersistenceMapper();

    TeacherPersistenceAdapter adapter() {
        return new TeacherPersistenceAdapter(jpaRepository, mapper);
    }

    Teacher sampleTeacher() {
        return Teacher.restore("uid-1", "Ana", "Soto", "a@x.com",
            AuthProvider.EMAIL_PASSWORD, OffsetDateTime.now(), OffsetDateTime.now(),
            null, false, null, null, null, null);
    }

    TeacherJpaEntity sampleJpaEntity() {
        TeacherJpaEntity e = new TeacherJpaEntity();
        e.setFirebaseUid("uid-1"); e.setFirstName("Ana"); e.setLastName("Soto");
        e.setEmail("a@x.com"); e.setProvider("EMAIL_PASSWORD");
        e.setCreatedAt(OffsetDateTime.now()); e.setUpdatedAt(OffsetDateTime.now());
        e.setRelatedParty(false);
        return e;
    }

    @Test
    void shouldMapDomainToEntityAndDelegateToJpaRepositoryWhenSaving() {
        adapter().save(sampleTeacher());

        ArgumentCaptor<TeacherJpaEntity> captor = ArgumentCaptor.forClass(TeacherJpaEntity.class);
        verify(jpaRepository).save(captor.capture());
        assertThat(captor.getValue().getFirebaseUid()).isEqualTo("uid-1");
        assertThat(captor.getValue().getEmail()).isEqualTo("a@x.com");
        assertThat(captor.getValue().getProvider()).isEqualTo("EMAIL_PASSWORD");
    }

    @Test
    void shouldMapEntityToDomainWhenTeacherFoundById() {
        when(jpaRepository.findById("uid-1")).thenReturn(Optional.of(sampleJpaEntity()));

        Optional<Teacher> result = adapter().findById("uid-1");

        assertThat(result).isPresent();
        assertThat(result.get().getFirebaseUid()).isEqualTo("uid-1");
        assertThat(result.get().getAuthProvider()).isEqualTo(AuthProvider.EMAIL_PASSWORD);
    }

    @Test
    void shouldReturnEmptyWhenTeacherNotFoundById() {
        when(jpaRepository.findById("missing")).thenReturn(Optional.empty());

        assertThat(adapter().findById("missing")).isEmpty();
    }

    @Test
    void shouldMapEntityToDomainWhenTeacherFoundByEmail() {
        when(jpaRepository.findByEmail("a@x.com")).thenReturn(Optional.of(sampleJpaEntity()));

        Optional<Teacher> result = adapter().findByEmail("a@x.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("a@x.com");
    }

    @Test
    void shouldReturnTrueWhenEmailExistsInRepository() {
        when(jpaRepository.existsByEmail("a@x.com")).thenReturn(true);

        assertThat(adapter().existsByEmail("a@x.com")).isTrue();
    }
}
