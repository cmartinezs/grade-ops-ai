package cl.gradeops.ai.api.auth.infrastructure.adapter.out.persistence;

import cl.gradeops.ai.api.auth.application.port.out.TeacherRepositoryPort;
import cl.gradeops.ai.api.domain.teacher.TeacherEntity;
import cl.gradeops.ai.api.domain.teacher.TeacherRepository;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class TeacherJpaRepositoryAdapter implements TeacherRepositoryPort {

    private final TeacherRepository teacherRepository;

    @Override
    public boolean existsById(String uid) {
        return teacherRepository.existsById(uid);
    }

    @Override
    public Optional<TeacherEntity> findById(String uid) {
        return teacherRepository.findById(uid);
    }

    @Override
    public Optional<TeacherEntity> findByEmail(String email) {
        return teacherRepository.findByEmail(email);
    }

    @Override
    public void save(TeacherEntity entity) {
        teacherRepository.save(entity);
    }
}
