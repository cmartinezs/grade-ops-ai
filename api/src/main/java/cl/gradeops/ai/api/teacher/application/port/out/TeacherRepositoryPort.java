package cl.gradeops.ai.api.teacher.application.port.out;

import cl.gradeops.ai.api.teacher.domain.model.Teacher;

import java.util.Optional;

public interface TeacherRepositoryPort {
    void save(Teacher teacher);
    Optional<Teacher> findById(String firebaseUid);
    Optional<Teacher> findByEmail(String email);
    boolean existsByEmail(String email);
}
