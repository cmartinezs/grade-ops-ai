package cl.gradeops.ai.api.auth.application.port.out;

import cl.gradeops.ai.api.auth.domain.model.TeacherIdentity;

public interface AuthPort {
    TeacherIdentity verifyToken(String idToken);
    TeacherIdentity verifyTokenUnchecked(String idToken);
    void revokeRefreshTokens(String uid);
    void updatePassword(String uid, String newPassword);
}
