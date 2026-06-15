package cl.gradeops.ai.api.domain.teacher;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeacherRepository extends JpaRepository<TeacherEntity, String> {
    Optional<TeacherEntity> findByEmail(String email);
    boolean existsByEmail(String email);
    List<TeacherEntity> findByPlanType(String planType);
    List<TeacherEntity> findByRelatedParty(boolean relatedParty);
}
