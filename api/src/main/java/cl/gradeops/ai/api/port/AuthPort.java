package cl.gradeops.ai.api.port;

public interface AuthPort {
    // Verifies token and checks revocation — used by FirebaseTokenFilter
    TeacherIdentity verifyToken(String idToken);

    // Verifies token without revocation check — used at registration time
    TeacherIdentity verifyTokenUnchecked(String idToken);

    void revokeRefreshTokens(String uid);
}
