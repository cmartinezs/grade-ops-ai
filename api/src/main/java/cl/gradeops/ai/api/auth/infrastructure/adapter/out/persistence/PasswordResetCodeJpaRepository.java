package cl.gradeops.ai.api.auth.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetCodeJpaRepository extends JpaRepository<PasswordResetCodeJpaEntity, UUID> {
    Optional<PasswordResetCodeJpaEntity> findByRawCode(String rawCode);
    Optional<PasswordResetCodeJpaEntity> findByTeacherUid(String teacherUid);
    void deleteByTeacherUid(String teacherUid);
}
