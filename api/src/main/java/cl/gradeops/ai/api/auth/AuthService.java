package cl.gradeops.ai.api.auth;

import cl.gradeops.ai.api.domain.teacher.TeacherEntity;
import cl.gradeops.ai.api.domain.teacher.TeacherRepository;
import cl.gradeops.ai.api.port.AuthPort;
import cl.gradeops.ai.api.port.TeacherIdentity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthPort authPort;
    private final TeacherRepository teacherRepository;

    public AuthService(AuthPort authPort, TeacherRepository teacherRepository) {
        this.authPort = authPort;
        this.teacherRepository = teacherRepository;
    }

    public void signOut(String uid) {
        authPort.revokeRefreshTokens(uid);
    }

    @Transactional
    public RegisterResult register(RegisterRequest request) {
        TeacherIdentity identity;
        try {
            identity = authPort.verifyTokenUnchecked(request.idToken());
        } catch (IllegalArgumentException ex) {
            throw new InvalidTokenException("Firebase ID token is invalid or expired");
        }

        String name = (request.name() != null && !request.name().isBlank())
                ? request.name()
                : (identity.name() != null ? identity.name() : identity.email());

        if (teacherRepository.existsById(identity.uid())) {
            return new RegisterResult(identity.uid(), false);
        }

        teacherRepository.save(new TeacherEntity(
                identity.uid(), name, identity.email(), identity.signInProvider()));
        return new RegisterResult(identity.uid(), true);
    }
}
