package cl.gradeops.ai.api.adapter.auth;

import cl.gradeops.ai.api.port.AuthPort;
import cl.gradeops.ai.api.port.TeacherIdentity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FirebaseAuthAdapter implements AuthPort {

    private final FirebaseAuth firebaseAuth;

    public FirebaseAuthAdapter(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    @Override
    public TeacherIdentity verifyToken(String idToken) {
        try {
            return toIdentity(firebaseAuth.verifyIdToken(idToken, true));
        } catch (FirebaseAuthException e) {
            throw new IllegalArgumentException("Invalid or revoked token", e);
        }
    }

    @Override
    public TeacherIdentity verifyTokenUnchecked(String idToken) {
        try {
            return toIdentity(firebaseAuth.verifyIdToken(idToken));
        } catch (FirebaseAuthException e) {
            throw new IllegalArgumentException("Invalid token", e);
        }
    }

    @Override
    public void revokeRefreshTokens(String uid) {
        try {
            firebaseAuth.revokeRefreshTokens(uid);
        } catch (FirebaseAuthException e) {
            throw new RuntimeException("Failed to revoke refresh tokens", e);
        }
    }

    @SuppressWarnings("unchecked")
    private TeacherIdentity toIdentity(FirebaseToken token) {
        String provider = "EMAIL_PASSWORD";
        Map<String, Object> claims = token.getClaims();
        if (claims != null) {
            Map<String, Object> firebase = (Map<String, Object>) claims.get("firebase");
            if (firebase != null && "google.com".equals(firebase.get("sign_in_provider"))) {
                provider = "GOOGLE";
            }
        }
        return new TeacherIdentity(
                token.getUid(),
                token.getEmail(),
                token.isEmailVerified(),
                token.getName(),
                provider
        );
    }
}
