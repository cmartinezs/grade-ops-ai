package cl.gradeops.ai.api.auth;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import cl.gradeops.ai.api.config.FirebaseTestConfig;
import cl.gradeops.ai.api.domain.teacher.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(FirebaseTestConfig.class)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired FirebaseAuth firebaseAuth;
    @Autowired TeacherRepository teacherRepository;

    @BeforeEach
    void setUp() {
        teacherRepository.deleteAll();
        reset(firebaseAuth);
    }

    @Test
    void valid_token_creates_teacher_and_returns_201() throws Exception {
        FirebaseToken mockToken = mock(FirebaseToken.class);
        when(mockToken.getUid()).thenReturn("uid-abc");
        when(mockToken.getEmail()).thenReturn("teacher@school.com");
        when(mockToken.getName()).thenReturn("Grace Hopper");
        when(firebaseAuth.verifyIdToken("valid-token")).thenReturn(mockToken);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idToken": "valid-token", "name": "Grace Hopper"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firebaseUid").value("uid-abc"));

        assertThat(teacherRepository.existsByEmail("teacher@school.com")).isTrue();
    }

    @Test
    void invalid_token_returns_401() throws Exception {
        when(firebaseAuth.verifyIdToken(anyString()))
                .thenThrow(mock(FirebaseAuthException.class));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idToken": "bad-token", "name": "Nobody"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INVALID_TOKEN"));
    }

    @Test
    void blank_idToken_returns_422_with_validation_error() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idToken": "  ", "name": "Teacher"}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void missing_idToken_returns_422_with_validation_error() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Teacher"}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void invalid_token_response_has_error_and_no_message_field() throws Exception {
        when(firebaseAuth.verifyIdToken(anyString()))
                .thenThrow(mock(FirebaseAuthException.class));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idToken": "bad", "name": "X"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INVALID_TOKEN"))
                .andExpect(jsonPath("$.message").doesNotExist());
    }

    @Test
    void sign_out_with_valid_token_returns_204_and_revokes_tokens() throws Exception {
        FirebaseToken mockToken = mock(FirebaseToken.class);
        when(mockToken.getUid()).thenReturn("uid-signout");
        when(mockToken.getEmail()).thenReturn("teacher@school.com");
        when(mockToken.isEmailVerified()).thenReturn(true);
        when(firebaseAuth.verifyIdToken("valid-signout-token", true)).thenReturn(mockToken);

        mockMvc.perform(post("/api/v1/auth/sign-out")
                        .header("Authorization", "Bearer valid-signout-token"))
                .andExpect(status().isNoContent());

        verify(firebaseAuth).revokeRefreshTokens("uid-signout");
    }

    @Test
    void sign_out_unauthenticated_returns_401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/sign-out"))
                .andExpect(status().isUnauthorized());

        verify(firebaseAuth, never()).revokeRefreshTokens(anyString());
    }

    @Test
    void re_registration_same_uid_returns_200_and_no_duplicate() throws Exception {
        FirebaseToken mockToken = mock(FirebaseToken.class);
        when(mockToken.getUid()).thenReturn("uid-abc");
        when(mockToken.getEmail()).thenReturn("teacher@school.com");
        when(mockToken.getName()).thenReturn("Grace Hopper");
        when(firebaseAuth.verifyIdToken("valid-token")).thenReturn(mockToken);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idToken": "valid-token", "name": "Grace Hopper"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idToken": "valid-token", "name": "Grace Hopper"}
                                """))
                .andExpect(status().isOk());

        assertThat(teacherRepository.findAll()).hasSize(1);
    }

    @Test
    void google_sign_in_first_time_creates_teacher_and_returns_201() throws Exception {
        FirebaseToken mockToken = mock(FirebaseToken.class);
        when(mockToken.getUid()).thenReturn("uid-google-1");
        when(mockToken.getEmail()).thenReturn("teacher@gmail.com");
        when(mockToken.getName()).thenReturn("Ada Lovelace");
        when(mockToken.getClaims()).thenReturn(Map.of(
                "firebase", Map.of("sign_in_provider", "google.com")));
        when(firebaseAuth.verifyIdToken("google-token")).thenReturn(mockToken);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idToken": "google-token", "name": null}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firebaseUid").value("uid-google-1"));

        var teacher = teacherRepository.findById("uid-google-1").orElseThrow();
        assertThat(teacher.getProvider()).isEqualTo("GOOGLE");
        assertThat(teacher.getName()).isEqualTo("Ada Lovelace");
    }

    @Test
    void google_sign_in_returning_user_returns_200_and_no_duplicate() throws Exception {
        FirebaseToken mockToken = mock(FirebaseToken.class);
        when(mockToken.getUid()).thenReturn("uid-google-2");
        when(mockToken.getEmail()).thenReturn("returning@gmail.com");
        when(mockToken.getName()).thenReturn("Alan Turing");
        when(mockToken.getClaims()).thenReturn(Map.of(
                "firebase", Map.of("sign_in_provider", "google.com")));
        when(firebaseAuth.verifyIdToken("google-token-2")).thenReturn(mockToken);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idToken": "google-token-2", "name": null}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idToken": "google-token-2", "name": null}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firebaseUid").value("uid-google-2"));

        assertThat(teacherRepository.findAll()).hasSize(1);
    }
}
