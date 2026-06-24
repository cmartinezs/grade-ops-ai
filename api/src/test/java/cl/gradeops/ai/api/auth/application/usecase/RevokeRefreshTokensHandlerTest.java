package cl.gradeops.ai.api.auth.application.usecase;

import cl.gradeops.ai.api.auth.application.port.out.AuthPort;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class RevokeRefreshTokensHandlerTest {

    private final AuthPort authPort = mock(AuthPort.class);
    private final RevokeRefreshTokensHandler handler = new RevokeRefreshTokensHandler(authPort);

    @Test
    void delegates_to_authPort_revokeRefreshTokens() {
        handler.execute("uid-1");
        verify(authPort).revokeRefreshTokens("uid-1");
    }
}
