package cl.gradeops.ai.api.teacher.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeacherJpaRepository extends JpaRepository<TeacherJpaEntity, String> {
    Optional<TeacherJpaEntity> findByEmail(String email);
    boolean existsByEmail(String email);
}
