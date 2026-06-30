package cl.gradeops.ai.api.auth.application.port.out;

import cl.gradeops.ai.api.auth.domain.model.PasswordResetCode;
import java.time.Instant;
import java.util.Optional;

public interface PasswordResetCodeRepositoryPort {
    void save(PasswordResetCode code);
    Optional<PasswordResetCode> findByRawCode(String rawCode);
    void deleteByTeacherUid(String teacherUid);
    long deleteAllClosedCreatedBefore(Instant threshold);
}
