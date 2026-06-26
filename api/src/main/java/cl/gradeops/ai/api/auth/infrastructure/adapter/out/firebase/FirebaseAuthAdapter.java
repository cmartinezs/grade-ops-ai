package cl.gradeops.ai.api.auth.infrastructure.adapter.out.firebase;

import cl.gradeops.ai.api.auth.application.port.out.AuthPort;
import cl.gradeops.ai.api.auth.domain.model.SignInProvider;
import cl.gradeops.ai.api.auth.domain.model.TeacherIdentity;
import cl.gradeops.ai.api.auth.infrastructure.exception.AuthProviderException;
import cl.gradeops.ai.api.shared.domain.exception.DuplicateEmailException;
import cl.gradeops.ai.api.shared.domain.exception.InvalidTokenException;
import com.google.firebase.auth.AuthErrorCode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;

import java.util.Map;

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
            throw new InvalidTokenException("Invalid or revoked token");
        }
    }

    @Override
    public TeacherIdentity verifyTokenUnchecked(String idToken) {
        try {
            return toIdentity(firebaseAuth.verifyIdToken(idToken));
        } catch (FirebaseAuthException e) {
            throw new InvalidTokenException("Invalid token");
        }
    }

    @Override
    public void revokeRefreshTokens(String uid) {
        try {
            firebaseAuth.revokeRefreshTokens(uid);
        } catch (FirebaseAuthException e) {
            throw new AuthProviderException("Failed to revoke refresh tokens for uid=" + uid, e);
        }
    }

    @Override
    public void updatePassword(String uid, String newPassword) {
        try {
            firebaseAuth.updateUser(new UserRecord.UpdateRequest(uid).setPassword(newPassword));
        } catch (FirebaseAuthException e) {
            throw new AuthProviderException("Failed to update password for uid=" + uid, e);
        }
    }

    @Override
    public String createUser(String email, String displayName) {
        try {
            UserRecord.CreateRequest req = new UserRecord.CreateRequest()
                .setEmail(email)
                .setDisplayName(displayName)
                .setEmailVerified(true);
            return firebaseAuth.createUser(req).getUid();
        } catch (FirebaseAuthException ex) {
            if (AuthErrorCode.EMAIL_ALREADY_EXISTS.equals(ex.getAuthErrorCode())) {
                throw new DuplicateEmailException(email);
            }
            throw new AuthProviderException("Firebase user creation failed for email=" + email, ex);
        }
    }

    @Override
    public void deleteUser(String uid) {
        try {
            firebaseAuth.deleteUser(uid);
        } catch (FirebaseAuthException ex) {
            throw new AuthProviderException("Failed to delete Firebase user: uid=" + uid, ex);
        }
    }

    @SuppressWarnings("unchecked")
    private TeacherIdentity toIdentity(FirebaseToken token) {
        SignInProvider provider = SignInProvider.EMAIL_PASSWORD;
        Map<String, Object> claims = token.getClaims();
        if (claims != null) {
            Map<String, Object> firebase = (Map<String, Object>) claims.get("firebase");
            if (firebase != null && "google.com".equals(firebase.get("sign_in_provider"))) {
                provider = SignInProvider.GOOGLE;
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
