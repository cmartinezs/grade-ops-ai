package cl.gradeops.ai.api.teacher.infrastructure.adapter.out.persistence;

import cl.gradeops.ai.api.teacher.application.port.out.TeacherRepositoryPort;
import cl.gradeops.ai.api.teacher.domain.model.Teacher;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class TeacherPersistenceAdapter implements TeacherRepositoryPort {

    private final TeacherJpaRepository jpaRepository;
    private final TeacherPersistenceMapper mapper;

    @Override
    public void save(Teacher teacher) {
        jpaRepository.save(mapper.toJpa(teacher));
    }

    @Override
    public Optional<Teacher> findById(String firebaseUid) {
        return jpaRepository.findById(firebaseUid).map(mapper::toDomain);
    }

    @Override
    public Optional<Teacher> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }
}
