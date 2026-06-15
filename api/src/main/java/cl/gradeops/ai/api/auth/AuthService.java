package cl.gradeops.ai.api.auth;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import cl.gradeops.ai.api.domain.teacher.TeacherEntity;
import cl.gradeops.ai.api.domain.teacher.TeacherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class AuthService {

    private final FirebaseAuth firebaseAuth;
    private final TeacherRepository teacherRepository;

    public AuthService(FirebaseAuth firebaseAuth, TeacherRepository teacherRepository) {
        this.firebaseAuth = firebaseAuth;
        this.teacherRepository = teacherRepository;
    }

    public void signOut(String uid) {
        try {
            firebaseAuth.revokeRefreshTokens(uid);
        } catch (FirebaseAuthException ex) {
            throw new RuntimeException("Failed to revoke refresh tokens", ex);
        }
    }

    @Transactional
    public RegisterResult register(RegisterRequest request) {
        FirebaseToken token;
        try {
            token = firebaseAuth.verifyIdToken(request.idToken());
        } catch (FirebaseAuthException ex) {
            throw new InvalidTokenException("Firebase ID token is invalid or expired");
        }

        String uid = token.getUid();
        String email = token.getEmail();
        String name = (request.name() != null && !request.name().isBlank())
                ? request.name()
                : (token.getName() != null ? token.getName() : email);
        String provider = resolveProvider(token);

        if (teacherRepository.existsById(uid)) {
            return new RegisterResult(uid, false);
        }

        teacherRepository.save(new TeacherEntity(uid, name, email, provider));
        return new RegisterResult(uid, true);
    }

    @SuppressWarnings("unchecked")
    private String resolveProvider(FirebaseToken token) {
        Map<String, Object> claims = token.getClaims();
        if (claims != null) {
            Map<String, Object> firebase = (Map<String, Object>) claims.get("firebase");
            if (firebase != null) {
                String signInProvider = (String) firebase.get("sign_in_provider");
                if ("google.com".equals(signInProvider)) {
                    return "GOOGLE";
                }
            }
        }
        return "EMAIL_PASSWORD";
    }
}
