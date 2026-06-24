package cl.gradeops.ai.api.auth.application.usecase;

import cl.gradeops.ai.api.auth.application.port.in.RevokeRefreshTokensUseCase;
import cl.gradeops.ai.api.auth.application.port.out.AuthPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class RevokeRefreshTokensHandler implements RevokeRefreshTokensUseCase {

    private final AuthPort authPort;

    @Override
    @Transactional
    public void execute(String uid) {
        authPort.revokeRefreshTokens(uid);
    }
}
