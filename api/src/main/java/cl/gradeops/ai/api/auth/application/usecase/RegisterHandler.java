package cl.gradeops.ai.api.auth.application.usecase;

import cl.gradeops.ai.api.auth.application.command.RegisterCommand;
import cl.gradeops.ai.api.auth.application.port.in.RegisterUseCase;
import cl.gradeops.ai.api.auth.application.port.out.AuthPort;
import cl.gradeops.ai.api.auth.application.port.out.TeacherRepositoryPort;
import cl.gradeops.ai.api.auth.application.result.RegisterResult;
import cl.gradeops.ai.api.auth.domain.model.TeacherIdentity;
import cl.gradeops.ai.api.domain.teacher.TeacherEntity;
import cl.gradeops.ai.api.shared.domain.exception.InvalidTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class RegisterHandler implements RegisterUseCase {

    private final AuthPort authPort;
    private final TeacherRepositoryPort teacherRepository;

    @Override
    @Transactional
    public RegisterResult execute(RegisterCommand command) {
        TeacherIdentity identity;
        try {
            identity = authPort.verifyTokenUnchecked(command.idToken());
        } catch (IllegalArgumentException ex) {
            throw new InvalidTokenException("Firebase ID token is invalid or expired");
        }

        String[] names = resolveNames(command, identity);
        String firstName = names[0];
        String lastName = names[1];

        if (teacherRepository.existsById(identity.uid())) {
            return new RegisterResult(identity.uid(), false);
        }

        teacherRepository.save(new TeacherEntity(
                identity.uid(), firstName, lastName, identity.email(), identity.signInProvider().name()));
        return new RegisterResult(identity.uid(), true);
    }

    private String[] resolveNames(RegisterCommand command, TeacherIdentity identity) {
        boolean hasFirst = command.firstName() != null && !command.firstName().isBlank();
        boolean hasLast  = command.lastName()  != null && !command.lastName().isBlank();
        if (hasFirst || hasLast) {
            return new String[]{
                hasFirst ? command.firstName().trim() : "",
                hasLast  ? command.lastName().trim()  : ""
            };
        }
        String displayName = identity.name() != null ? identity.name().trim() : identity.email();
        int space = displayName.indexOf(' ');
        if (space < 0) return new String[]{displayName, ""};
        return new String[]{displayName.substring(0, space), displayName.substring(space + 1)};
    }
}
