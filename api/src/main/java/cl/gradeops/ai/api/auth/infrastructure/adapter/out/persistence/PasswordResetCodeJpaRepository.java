package cl.gradeops.ai.api.auth.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetCodeJpaRepository extends JpaRepository<PasswordResetCodeJpaEntity, UUID> {
    Optional<PasswordResetCodeJpaEntity> findByRawCode(String rawCode);
    Optional<PasswordResetCodeJpaEntity> findByTeacherUid(String teacherUid);
    void deleteByTeacherUid(String teacherUid);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("""
        DELETE FROM PasswordResetCodeJpaEntity e
        WHERE e.createdAt < :threshold
          AND (e.usedAt IS NOT NULL OR e.expiresAt < CURRENT_TIMESTAMP)
        """)
    int bulkDeleteClosedCreatedBefore(@Param("threshold") Instant threshold);
}
