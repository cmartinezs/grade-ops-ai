package cl.gradeops.ai.api.auth.application.usecase;

import cl.gradeops.ai.api.auth.application.port.in.RevokeRefreshTokensUseCase;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class SignOutHandlerTest {

    private final RevokeRefreshTokensUseCase revokeRefreshTokensUseCase = mock(RevokeRefreshTokensUseCase.class);
    private final SignOutHandler handler = new SignOutHandler(revokeRefreshTokensUseCase);

    @Test
    void delegates_to_revokeRefreshTokensUseCase() {
        handler.execute("uid-1");
        verify(revokeRefreshTokensUseCase).execute("uid-1");
    }
}
