package cl.gradeops.ai.api.auth.infrastructure.adapter.out.persistence;

import cl.gradeops.ai.api.auth.domain.model.PasswordResetCode;
import cl.gradeops.ai.api.auth.domain.valueobject.RawCode;

public class PasswordResetCodePersistenceMapper {

    PasswordResetCode toDomain(PasswordResetCodeJpaEntity entity) {
        return PasswordResetCode.restore(
            entity.getTeacherUid(),
            new RawCode(entity.getRawCode()),
            entity.getExpiresAt(),
            entity.getCreatedAt(),
            entity.getUsedAt()
        );
    }

    PasswordResetCodeJpaEntity toEntity(PasswordResetCode code) {
        PasswordResetCodeJpaEntity entity = new PasswordResetCodeJpaEntity();
        entity.setTeacherUid(code.getTeacherUid());
        entity.setRawCode(code.getRawCode().value());
        entity.setExpiresAt(code.getExpiresAt());
        entity.setCreatedAt(code.getCreatedAt());
        entity.setUsedAt(code.getUsedAt());
        return entity;
    }

    void updateEntity(PasswordResetCodeJpaEntity entity, PasswordResetCode code) {
        entity.setUsedAt(code.getUsedAt());
    }
}
