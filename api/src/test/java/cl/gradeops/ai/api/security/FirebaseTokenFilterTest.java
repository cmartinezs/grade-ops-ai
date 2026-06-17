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

import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(FirebaseTestConfig.class)
class FirebaseTokenFilterTest {

    @Autowired MockMvc mockMvc;
    @Autowired FirebaseAuth firebaseAuth;

    @BeforeEach
    void setUp() {
        reset(firebaseAuth);
        SecurityContextHolder.clearContext();
    }

    @Test
    void valid_token_sets_authenticated_teacher_principal() throws Exception {
        FirebaseToken mockToken = buildMockToken("uid-teacher-1", "teacher@school.com", true);
        when(firebaseAuth.verifyIdToken("valid-token", true)).thenReturn(mockToken);
        when(firebaseAuth.verifyIdToken("valid-token")).thenReturn(mockToken);

        mockMvc.perform(post("/api/v1/auth/register")
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

        mockMvc.perform(get("/some/protected/resource")
                        .header("Authorization", "Bearer revoked-token"))
                .andExpect(status().isUnauthorized());

        verify(firebaseAuth).verifyIdToken("revoked-token", true);
    }

    @Test
    void missing_authorization_header_public_path_returns_200_and_protected_returns_401()
            throws Exception {
        FirebaseToken mockToken = buildMockToken("uid-no-header", "noheader@school.com", true);
        when(firebaseAuth.verifyIdToken("any-token")).thenReturn(mockToken);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content("""
                                {"idToken": "any-token", "name": "NoHeader"}
                                """))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(get("/some/protected/resource"))
                .andExpect(status().isUnauthorized());

        verify(firebaseAuth).verifyIdToken("any-token");
        verifyNoMoreInteractions(firebaseAuth);
    }

    @Test
    void malformed_token_clears_context_and_does_not_propagate_exception() throws Exception {
        when(firebaseAuth.verifyIdToken("malformed", true))
                .thenThrow(new IllegalArgumentException("bad token format"));

        mockMvc.perform(get("/some/protected/resource")
                        .header("Authorization", "Bearer malformed"))
                .andExpect(status().isUnauthorized());

        verify(firebaseAuth).verifyIdToken("malformed", true);
    }

    private FirebaseToken buildMockToken(String uid, String email, boolean emailVerified) {
        FirebaseToken token = mock(FirebaseToken.class);
        when(token.getUid()).thenReturn(uid);
        when(token.getEmail()).thenReturn(email);
        when(token.isEmailVerified()).thenReturn(emailVerified);
        when(token.getName()).thenReturn("Test Teacher");
        when(token.getClaims()).thenReturn(Map.of(
                "firebase", Map.of("sign_in_provider", "password")));
        return token;
    }
}
