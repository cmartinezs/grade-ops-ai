package cl.gradeops.ai.api.auth.application.usecase;

import cl.gradeops.ai.api.auth.application.port.in.RevokeRefreshTokensUseCase;
import cl.gradeops.ai.api.auth.application.port.in.SignOutUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class SignOutHandler implements SignOutUseCase {

    private final RevokeRefreshTokensUseCase revokeRefreshTokensUseCase;

    @Override
    @Transactional
    public void execute(String uid) {
        revokeRefreshTokensUseCase.execute(uid);
    }
}
