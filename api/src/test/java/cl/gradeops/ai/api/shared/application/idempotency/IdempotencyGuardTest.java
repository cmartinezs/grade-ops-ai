package cl.gradeops.ai.api.shared.application.idempotency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdempotencyGuardTest {

    @Mock IdempotencyRepositoryPort repository;

    IdempotencyGuard guard;

    static final String OPERATION_TYPE = "CREATE_INITIAL_REVISION";
    static final String KEY = "key-1";
    static final String HASH = "hash-1";

    @BeforeEach
    void setUp() {
        guard = new IdempotencyGuard(repository);
    }

    @Test
    void shouldReturnEmptyWhenNoPriorRecordExistsForTeacherScope() {
        IdempotencyScope scope = IdempotencyScope.teacher("uid-1");
        when(repository.find(scope, OPERATION_TYPE, KEY)).thenReturn(Optional.empty());

        Optional<IdempotencyRecord> result = guard.check(scope, OPERATION_TYPE, KEY, HASH);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenNoPriorRecordExistsForAssessmentScope() {
        IdempotencyScope scope = IdempotencyScope.assessment(UUID.randomUUID());
        when(repository.find(scope, OPERATION_TYPE, KEY)).thenReturn(Optional.empty());

        Optional<IdempotencyRecord> result = guard.check(scope, OPERATION_TYPE, KEY, HASH);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReplayPriorRecordWhenSameKeyAndSamePayloadHash() {
        IdempotencyScope scope = IdempotencyScope.assessment(UUID.randomUUID());
        IdempotencyRecord prior = IdempotencyRecord.create(scope, OPERATION_TYPE, KEY, HASH, "ref-1", 201);
        when(repository.find(scope, OPERATION_TYPE, KEY)).thenReturn(Optional.of(prior));

        Optional<IdempotencyRecord> result = guard.check(scope, OPERATION_TYPE, KEY, HASH);

        assertThat(result).contains(prior);
    }

    @Test
    void shouldThrowPayloadMismatchWhenSameKeyButDifferentPayloadHash() {
        IdempotencyScope scope = IdempotencyScope.assessment(UUID.randomUUID());
        IdempotencyRecord prior = IdempotencyRecord.create(scope, OPERATION_TYPE, KEY, HASH, "ref-1", 201);
        when(repository.find(scope, OPERATION_TYPE, KEY)).thenReturn(Optional.of(prior));

        assertThatThrownBy(() -> guard.check(scope, OPERATION_TYPE, KEY, "different-hash"))
                .isInstanceOf(IdempotencyKeyPayloadMismatchException.class);
    }

    @Test
    void shouldPersistNewRecordWithComputedExpiry() {
        IdempotencyScope scope = IdempotencyScope.assessment(UUID.randomUUID());

        guard.record(scope, OPERATION_TYPE, KEY, HASH, "ref-1", 201);

        ArgumentCaptor<IdempotencyRecord> captor = ArgumentCaptor.forClass(IdempotencyRecord.class);
        verify(repository).save(captor.capture());
        IdempotencyRecord saved = captor.getValue();
        assertThat(saved.getScope()).isEqualTo(scope);
        assertThat(saved.getOperationType()).isEqualTo(OPERATION_TYPE);
        assertThat(saved.getIdempotencyKey()).isEqualTo(KEY);
        assertThat(saved.getRequestPayloadHash()).isEqualTo(HASH);
        assertThat(saved.getResultReference()).isEqualTo("ref-1");
        assertThat(saved.getResponseStatus()).isEqualTo(201);
        assertThat(saved.getExpiresAt()).isAfter(saved.getCreatedAt());
    }
}
