package cl.gradeops.ai.api.auth.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetCodeJpaRepository extends JpaRepository<PasswordResetCodeJpaEntity, String> {
    Optional<PasswordResetCodeJpaEntity> findByRawCode(String rawCode);
    void deleteByTeacherUid(String teacherUid);
}
