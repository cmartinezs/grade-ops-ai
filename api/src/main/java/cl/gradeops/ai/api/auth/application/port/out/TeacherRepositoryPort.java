package cl.gradeops.ai.api.auth.application.port.out;

import cl.gradeops.ai.api.domain.teacher.TeacherEntity;

import java.util.Optional;

public interface TeacherRepositoryPort {
    boolean existsById(String uid);
    Optional<TeacherEntity> findById(String uid);
    Optional<TeacherEntity> findByEmail(String email);
    void save(TeacherEntity entity);
}
