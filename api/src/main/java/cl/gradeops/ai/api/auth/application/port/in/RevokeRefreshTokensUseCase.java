package cl.gradeops.ai.api.auth.application.port.in;

public interface RevokeRefreshTokensUseCase {
    void execute(String uid);
}
