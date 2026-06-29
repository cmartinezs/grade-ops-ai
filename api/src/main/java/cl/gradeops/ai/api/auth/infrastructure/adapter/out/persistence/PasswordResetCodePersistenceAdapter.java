package cl.gradeops.ai.api.auth.infrastructure.adapter.out.persistence;

import cl.gradeops.ai.api.auth.application.port.out.PasswordResetCodeRepositoryPort;
import cl.gradeops.ai.api.auth.domain.model.PasswordResetCode;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class PasswordResetCodePersistenceAdapter implements PasswordResetCodeRepositoryPort {

    private final PasswordResetCodeJpaRepository jpaRepository;
    private final PasswordResetCodePersistenceMapper mapper;

    @Override
    public void save(PasswordResetCode code) {
        PasswordResetCodeJpaEntity entity = jpaRepository
                .findByTeacherUid(code.getTeacherUid())
                .map(existing -> { mapper.updateEntity(existing, code); return existing; })
                .orElseGet(() -> mapper.toEntity(code));
        jpaRepository.save(entity);
    }

    @Override
    public Optional<PasswordResetCode> findByRawCode(String rawCode) {
        return jpaRepository.findByRawCode(rawCode).map(mapper::toDomain);
    }

    @Override
    public void deleteByTeacherUid(String teacherUid) {
        jpaRepository.deleteByTeacherUid(teacherUid);
    }
}
