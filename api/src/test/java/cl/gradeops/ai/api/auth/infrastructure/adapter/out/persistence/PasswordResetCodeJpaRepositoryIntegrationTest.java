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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates PasswordResetCodeJpaRepository against the real Flyway schema (V5–V7).
 * Covers: UUID PK generation by DB, Instant↔TIMESTAMPTZ round-trip, and derived queries.
 * Requires Docker. Does NOT activate the "test" profile (avoids H2 override).
 * Container lifecycle is managed by Ryuk (auto-cleanup after JVM exit).
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@TestPropertySource(properties = {
    "spring.flyway.enabled=true",
    "spring.jpa.hibernate.ddl-auto=none"
})
class PasswordResetCodeJpaRepositoryIntegrationTest {

    private static final String TEACHER_UID = "test-teacher-uid";

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

    @BeforeEach
    void insertTeacher() {
        // password_reset_codes.teacher_uid has an FK to teacher.firebase_uid (V5)
        // teacher schema after V1–V4: firebase_uid, first_name (was name), last_name, email
        jdbcTemplate.update(
            "INSERT INTO teacher (firebase_uid, first_name, last_name, email) VALUES (?, ?, ?, ?)",
            TEACHER_UID, "Test", "Teacher", "test@example.com");
    }

    @Test
    void shouldGenerateUuidWhenSavingNewRecord() {
        // given
        PasswordResetCodeJpaEntity entity = entityWith("code-gen");

        // when
        PasswordResetCodeJpaEntity saved = repository.saveAndFlush(entity);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTeacherUid()).isEqualTo(TEACHER_UID);
    }

    @Test
    void shouldPersistAllFieldsWithMicrosecondPrecision() {
        // given — truncate to MICROS: PostgreSQL TIMESTAMPTZ has microsecond precision
        Instant expiresAt = Instant.now().plus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.MICROS);
        Instant createdAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
        Instant usedAt   = Instant.now().plus(5, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MICROS);

        PasswordResetCodeJpaEntity entity = new PasswordResetCodeJpaEntity();
        entity.setTeacherUid(TEACHER_UID);
        entity.setRawCode("code-fields");
        entity.setExpiresAt(expiresAt);
        entity.setCreatedAt(createdAt);
        entity.setUsedAt(usedAt);

        // when
        repository.saveAndFlush(entity);
        PasswordResetCodeJpaEntity loaded = repository.findByTeacherUid(TEACHER_UID).orElseThrow();

        // then
        assertThat(loaded.getRawCode()).isEqualTo("code-fields");
        assertThat(loaded.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(loaded.getCreatedAt()).isEqualTo(createdAt);
        assertThat(loaded.getUsedAt()).isEqualTo(usedAt);
    }

    @Test
    void shouldFindByTeacherUidAfterSave() {
        // given
        repository.saveAndFlush(entityWith("code-find-teacher"));

        // when
        Optional<PasswordResetCodeJpaEntity> result = repository.findByTeacherUid(TEACHER_UID);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getRawCode()).isEqualTo("code-find-teacher");
    }

    @Test
    void shouldReturnEmptyWhenTeacherUidNotFound() {
        // when
        Optional<PasswordResetCodeJpaEntity> result = repository.findByTeacherUid("uid-nonexistent");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindByRawCodeAfterSave() {
        // given
        repository.saveAndFlush(entityWith("code-find-raw"));

        // when
        Optional<PasswordResetCodeJpaEntity> result = repository.findByRawCode("code-find-raw");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getTeacherUid()).isEqualTo(TEACHER_UID);
    }

    @Test
    void shouldDeleteByTeacherUidAndLeaveNoRecord() {
        // given
        repository.saveAndFlush(entityWith("code-delete"));
        assertThat(repository.findByTeacherUid(TEACHER_UID)).isPresent();

        // when
        repository.deleteByTeacherUid(TEACHER_UID);

        // then
        assertThat(repository.findByTeacherUid(TEACHER_UID)).isEmpty();
    }

    @Test
    void shouldHaveIndexOnCreatedAt() {
        // given — V8 migration creates idx_prc_created_at on password_reset_codes(created_at)
        String sql = """
                SELECT indexdef FROM pg_indexes
                WHERE tablename = 'password_reset_codes'
                  AND indexname  = 'idx_prc_created_at'
                """;

        // when
        String indexDef = jdbcTemplate.queryForObject(sql, String.class);

        // then
        assertThat(indexDef).isNotNull();
        assertThat(indexDef).containsIgnoringCase("created_at");
    }

    private PasswordResetCodeJpaEntity entityWith(String rawCode) {
        PasswordResetCodeJpaEntity e = new PasswordResetCodeJpaEntity();
        e.setTeacherUid(TEACHER_UID);
        e.setRawCode(rawCode);
        e.setExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.MICROS));
        e.setCreatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        return e;
    }
}
