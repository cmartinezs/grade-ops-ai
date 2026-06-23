package cl.gradeops.ai.api.auth;

import cl.gradeops.ai.api.domain.teacher.TeacherEntity;
import cl.gradeops.ai.api.domain.teacher.TeacherRepository;
import cl.gradeops.ai.api.port.AuthPort;
import cl.gradeops.ai.api.port.TeacherIdentity;
import cl.gradeops.ai.api.shared.infrastructure.adapter.in.web.InvalidTokenException;
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

    private String[] resolveNames(RegisterRequest request, cl.gradeops.ai.api.port.TeacherIdentity identity) {
        boolean hasFirst = request.firstName() != null && !request.firstName().isBlank();
        boolean hasLast  = request.lastName()  != null && !request.lastName().isBlank();
        if (hasFirst || hasLast) {
            return new String[]{
                hasFirst ? request.firstName().trim() : "",
                hasLast  ? request.lastName().trim()  : ""
            };
        }
        String displayName = identity.name() != null ? identity.name().trim() : identity.email();
        int space = displayName.indexOf(' ');
        if (space < 0) return new String[]{displayName, ""};
        return new String[]{displayName.substring(0, space), displayName.substring(space + 1)};
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

        String[] names = resolveNames(request, identity);
        String firstName = names[0];
        String lastName = names[1];

        if (teacherRepository.existsById(identity.uid())) {
            return new RegisterResult(identity.uid(), false);
        }

        teacherRepository.save(new TeacherEntity(
                identity.uid(), firstName, lastName, identity.email(), identity.signInProvider()));
        return new RegisterResult(identity.uid(), true);
    }
}
