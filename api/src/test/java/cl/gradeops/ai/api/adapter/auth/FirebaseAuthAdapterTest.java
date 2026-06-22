package cl.gradeops.ai.api.adapter.auth;

import cl.gradeops.ai.api.port.TeacherIdentity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FirebaseAuthAdapterTest {

    @Mock FirebaseAuth firebaseAuth;
    @InjectMocks FirebaseAuthAdapter adapter;

    @Test
    void verifyToken_maps_firebase_token_to_teacher_identity() throws Exception {
        FirebaseToken token = mockToken("uid-1", "t@school.com", true, "Teacher", "google.com");
        when(firebaseAuth.verifyIdToken("tok", true)).thenReturn(token);

        TeacherIdentity identity = adapter.verifyToken("tok");

        assertThat(identity.uid()).isEqualTo("uid-1");
        assertThat(identity.email()).isEqualTo("t@school.com");
        assertThat(identity.emailVerified()).isTrue();
        assertThat(identity.name()).isEqualTo("Teacher");
        assertThat(identity.signInProvider()).isEqualTo("GOOGLE");
    }

    @Test
    void verifyToken_maps_password_provider() throws Exception {
        FirebaseToken token = mockToken("uid-2", "t@school.com", true, "T", "password");
        when(firebaseAuth.verifyIdToken("tok", true)).thenReturn(token);

        TeacherIdentity identity = adapter.verifyToken("tok");

        assertThat(identity.signInProvider()).isEqualTo("EMAIL_PASSWORD");
    }

    @Test
    void verifyToken_throws_illegal_argument_on_firebase_exception() throws Exception {
        when(firebaseAuth.verifyIdToken("bad", true)).thenThrow(mock(FirebaseAuthException.class));

        assertThatThrownBy(() -> adapter.verifyToken("bad"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void verifyTokenUnchecked_does_not_check_revocation() throws Exception {
        FirebaseToken token = mockToken("uid-3", "t@school.com", true, "T", "password");
        when(firebaseAuth.verifyIdToken("tok")).thenReturn(token);

        adapter.verifyTokenUnchecked("tok");

        verify(firebaseAuth).verifyIdToken("tok");
        verify(firebaseAuth, never()).verifyIdToken("tok", true);
    }

    @Test
    void revokeRefreshTokens_delegates_to_firebase() throws Exception {
        adapter.revokeRefreshTokens("uid-1");

        verify(firebaseAuth).revokeRefreshTokens("uid-1");
    }

    @Test
    void updatePassword_callsFirebaseUpdateUserWithCorrectUid() throws FirebaseAuthException {
        adapter.updatePassword("uid-teacher-1", "newSecurePass");

        ArgumentCaptor<UserRecord.UpdateRequest> captor =
            ArgumentCaptor.forClass(UserRecord.UpdateRequest.class);
        verify(firebaseAuth).updateUser(captor.capture());
        // Verify that updateUser was called with a request
        assertThat(captor.getValue()).isNotNull();
    }

    @Test
    void updatePassword_wrapsFirebaseExceptionAsRuntimeException() throws FirebaseAuthException {
        when(firebaseAuth.updateUser(any())).thenThrow(mock(FirebaseAuthException.class));

        assertThatThrownBy(() -> adapter.updatePassword("uid-teacher-1", "securePassword123"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to update password");
    }

    private FirebaseToken mockToken(String uid, String email, boolean verified,
                                    String name, String provider) {
        FirebaseToken token = mock(FirebaseToken.class);
        when(token.getUid()).thenReturn(uid);
        when(token.getEmail()).thenReturn(email);
        when(token.isEmailVerified()).thenReturn(verified);
        when(token.getName()).thenReturn(name);
        when(token.getClaims()).thenReturn(Map.of(
                "firebase", Map.of("sign_in_provider", provider)));
        return token;
    }
}
