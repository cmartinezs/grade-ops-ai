package cl.gradeops.ai.api.auth.infrastructure.adapter.out.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for PasswordResetCodePersistenceAdapter.deleteAllClosedCreatedBefore.
 * Validates the JPQL bulk-delete query against a real Flyway schema (V1–V8).
 * Requires Docker. Each test runs in a rolled-back transaction — no teardown needed.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@TestPropertySource(properties = {
    "spring.flyway.enabled=true",
    "spring.jpa.hibernate.ddl-auto=none"
})
class PasswordResetCodePersistenceAdapterIntegrationTest {

    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @Autowired PasswordResetCodeJpaRepository repository;
    @Autowired JdbcTemplate jdbcTemplate;

    PasswordResetCodePersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new PasswordResetCodePersistenceAdapter(repository, new PasswordResetCodePersistenceMapper());
    }

    @Test
    void shouldDeleteUsedRecordBeyondRetention() {
        // given
        Instant threshold = Instant.now().minus(90, ChronoUnit.DAYS);
        insertTeacher("t1");
        repository.saveAndFlush(entity("t1", "code-1",
                Instant.now().minus(91, ChronoUnit.DAYS),   // created_at — before threshold
                Instant.now().plus(1, ChronoUnit.HOURS),    // expires_at — not yet expired
                Instant.now().minus(1, ChronoUnit.HOURS))); // used_at   — marked used

        // when
        long deleted = adapter.deleteAllClosedCreatedBefore(threshold);

        // then
        assertThat(deleted).isEqualTo(1L);
        assertThat(repository.count()).isZero();
    }

    @Test
    void shouldDeleteExpiredRecordBeyondRetention() {
        // given
        Instant threshold = Instant.now().minus(90, ChronoUnit.DAYS);
        insertTeacher("t2");
        repository.saveAndFlush(entity("t2", "code-2",
                Instant.now().minus(91, ChronoUnit.DAYS),  // created_at — before threshold
                Instant.now().minus(1, ChronoUnit.HOURS),  // expires_at — already expired
                null));                                     // used_at   — never used

        // when
        long deleted = adapter.deleteAllClosedCreatedBefore(threshold);

        // then
        assertThat(deleted).isEqualTo(1L);
        assertThat(repository.count()).isZero();
    }

    @Test
    void shouldNotDeleteRecentRecord() {
        // given — record is used but within retention window
        Instant threshold = Instant.now().minus(90, ChronoUnit.DAYS);
        insertTeacher("t3");
        repository.saveAndFlush(entity("t3", "code-3",
                Instant.now().minus(1, ChronoUnit.DAYS),    // created_at — within retention
                Instant.now().plus(1, ChronoUnit.HOURS),
                Instant.now().minus(1, ChronoUnit.HOURS)));

        // when
        long deleted = adapter.deleteAllClosedCreatedBefore(threshold);

        // then
        assertThat(deleted).isZero();
        assertThat(repository.count()).isEqualTo(1L);
    }

    @Test
    void shouldNotDeletePendingNotExpiredRecord() {
        // given — old record but still active (not used, not expired)
        Instant threshold = Instant.now().minus(90, ChronoUnit.DAYS);
        insertTeacher("t4");
        repository.saveAndFlush(entity("t4", "code-4",
                Instant.now().minus(91, ChronoUnit.DAYS),  // created_at — before threshold
                Instant.now().plus(1, ChronoUnit.HOURS),   // expires_at — still valid
                null));                                     // used_at   — pending

        // when
        long deleted = adapter.deleteAllClosedCreatedBefore(threshold);

        // then
        assertThat(deleted).isZero();
        assertThat(repository.count()).isEqualTo(1L);
    }

    @Test
    void shouldReturnCorrectCount() {
        // given — 3 eligible records + 2 non-eligible records
        Instant threshold = Instant.now().minus(90, ChronoUnit.DAYS);
        Instant old = Instant.now().minus(91, ChronoUnit.DAYS);
        Instant recent = Instant.now().minus(1, ChronoUnit.DAYS);

        // eligible #1 — used + old
        insertTeacher("tc1");
        repository.saveAndFlush(entity("tc1", "code-c1", old,
                Instant.now().plus(1, ChronoUnit.HOURS), Instant.now().minus(1, ChronoUnit.HOURS)));

        // eligible #2 — expired + old
        insertTeacher("tc2");
        repository.saveAndFlush(entity("tc2", "code-c2", old,
                Instant.now().minus(1, ChronoUnit.HOURS), null));

        // eligible #3 — used + old
        insertTeacher("tc3");
        repository.saveAndFlush(entity("tc3", "code-c3", old,
                Instant.now().plus(2, ChronoUnit.HOURS), Instant.now().minus(2, ChronoUnit.HOURS)));

        // not eligible #4 — used but recent
        insertTeacher("tc4");
        repository.saveAndFlush(entity("tc4", "code-c4", recent,
                Instant.now().plus(1, ChronoUnit.HOURS), Instant.now().minus(1, ChronoUnit.HOURS)));

        // not eligible #5 — pending (not expired), old
        insertTeacher("tc5");
        repository.saveAndFlush(entity("tc5", "code-c5", old,
                Instant.now().plus(1, ChronoUnit.HOURS), null));

        // when
        long deleted = adapter.deleteAllClosedCreatedBefore(threshold);

        // then
        assertThat(deleted).isEqualTo(3L);
        assertThat(repository.count()).isEqualTo(2L);
    }

    @Test
    void shouldBeIdempotent() {
        // given — one eligible record
        Instant threshold = Instant.now().minus(90, ChronoUnit.DAYS);
        insertTeacher("ti1");
        repository.saveAndFlush(entity("ti1", "code-i1",
                Instant.now().minus(91, ChronoUnit.DAYS),  // created_at — before threshold
                Instant.now().minus(1, ChronoUnit.HOURS),  // expires_at — expired
                null));                                     // used_at   — not used

        // when
        long firstCall = adapter.deleteAllClosedCreatedBefore(threshold);
        long secondCall = adapter.deleteAllClosedCreatedBefore(threshold);

        // then
        assertThat(firstCall).isEqualTo(1L);
        assertThat(secondCall).isZero();
    }

    private void insertTeacher(String uid) {
        jdbcTemplate.update(
                "INSERT INTO teacher (firebase_uid, first_name, last_name, email) VALUES (?, ?, ?, ?)",
                uid, "Test", "Teacher", uid + "@test.com");
    }

    private PasswordResetCodeJpaEntity entity(
            String teacherUid, String rawCode, Instant createdAt, Instant expiresAt, Instant usedAt) {
        PasswordResetCodeJpaEntity e = new PasswordResetCodeJpaEntity();
        e.setTeacherUid(teacherUid);
        e.setRawCode(rawCode);
        e.setCreatedAt(createdAt.truncatedTo(ChronoUnit.MICROS));
        e.setExpiresAt(expiresAt.truncatedTo(ChronoUnit.MICROS));
        e.setUsedAt(usedAt != null ? usedAt.truncatedTo(ChronoUnit.MICROS) : null);
        return e;
    }
}
