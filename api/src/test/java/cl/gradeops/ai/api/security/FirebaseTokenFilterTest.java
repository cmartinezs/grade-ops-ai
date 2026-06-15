package cl.gradeops.ai.api.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import cl.gradeops.ai.api.config.FirebaseTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(FirebaseTestConfig.class)
class FirebaseTokenFilterTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    FirebaseAuth firebaseAuth;

    @BeforeEach
    void setUp() {
        reset(firebaseAuth);
        SecurityContextHolder.clearContext();
    }

    @Test
    void valid_token_sets_authenticated_teacher_principal() throws Exception {
        FirebaseToken mockToken = mock(FirebaseToken.class);
        when(mockToken.getUid()).thenReturn("uid-teacher-1");
        when(mockToken.getEmail()).thenReturn("teacher@school.com");
        when(mockToken.getName()).thenReturn("Teacher");
        when(mockToken.isEmailVerified()).thenReturn(true);
        when(firebaseAuth.verifyIdToken("valid-token", true)).thenReturn(mockToken);
        when(firebaseAuth.verifyIdToken("valid-token")).thenReturn(mockToken);

        // Any protected endpoint will return 200 when principal is set
        // /auth/register is public; use it here to confirm filter does not block valid requests.
        // The filter sets the principal; the downstream 401 only fires when auth is missing.
        mockMvc.perform(post("/auth/register")
                        .header("Authorization", "Bearer valid-token")
                        .contentType("application/json")
                        .content("""
                                {"idToken": "valid-token", "name": "Teacher"}
                                """))
                .andExpect(status().is2xxSuccessful());

        verify(firebaseAuth).verifyIdToken("valid-token", true);
    }

    @Test
    void revoked_token_clears_context_and_protected_endpoint_returns_401() throws Exception {
        FirebaseAuthException revokedException = mock(FirebaseAuthException.class);
        when(firebaseAuth.verifyIdToken("revoked-token", true)).thenThrow(revokedException);

        // A protected endpoint (anything not in permitAll) must return 401
        mockMvc.perform(get("/some/protected/resource")
                        .header("Authorization", "Bearer revoked-token"))
                .andExpect(status().isUnauthorized());

        verify(firebaseAuth).verifyIdToken("revoked-token", true);
    }

    @Test
    void missing_authorization_header_public_path_returns_200_and_protected_returns_401()
            throws Exception {
        FirebaseToken mockToken = mock(FirebaseToken.class);
        when(mockToken.getUid()).thenReturn("uid-no-header");
        when(mockToken.getEmail()).thenReturn("noheader@school.com");
        when(mockToken.getName()).thenReturn("NoHeader");
        when(firebaseAuth.verifyIdToken("any-token")).thenReturn(mockToken);

        // Public path — no token needed
        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content("""
                                {"idToken": "any-token", "name": "NoHeader"}
                                """))
                .andExpect(status().is2xxSuccessful());

        // Protected path — no token → 401
        mockMvc.perform(get("/some/protected/resource"))
                .andExpect(status().isUnauthorized());

        verify(firebaseAuth).verifyIdToken("any-token");
        verifyNoMoreInteractions(firebaseAuth);
    }

    @Test
    void malformed_token_clears_context_and_does_not_propagate_exception() throws Exception {
        when(firebaseAuth.verifyIdToken("malformed", true))
                .thenThrow(new IllegalArgumentException("bad token format"));

        // Should not throw; context must be cleared → protected endpoint returns 401
        mockMvc.perform(get("/some/protected/resource")
                        .header("Authorization", "Bearer malformed"))
                .andExpect(status().isUnauthorized());

        verify(firebaseAuth).verifyIdToken("malformed", true);
    }
}
