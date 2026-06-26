package cl.gradeops.ai.api.teacher.application.usecase;

import cl.gradeops.ai.api.auth.application.command.IssuePasswordResetCodeCommand;
import cl.gradeops.ai.api.auth.application.port.in.IssuePasswordResetCodeUseCase;
import cl.gradeops.ai.api.auth.application.port.out.AuthPort;
import cl.gradeops.ai.api.auth.domain.model.SignInProvider;
import cl.gradeops.ai.api.shared.domain.exception.DuplicateEmailException;
import cl.gradeops.ai.api.teacher.application.command.ProvisionTeacherCommand;
import cl.gradeops.ai.api.teacher.application.port.in.ProvisionTeacherUseCase;
import cl.gradeops.ai.api.teacher.application.port.out.TeacherRepositoryPort;
import cl.gradeops.ai.api.teacher.application.result.ProvisionTeacherResult;
import cl.gradeops.ai.api.teacher.domain.model.AuthProvider;
import cl.gradeops.ai.api.teacher.domain.model.Teacher;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

// NO @Service — declared as @Bean in TeacherConfig
@RequiredArgsConstructor
public class ProvisionTeacherHandler implements ProvisionTeacherUseCase {

    private final AuthPort authPort;
    private final TeacherRepositoryPort teacherRepository;
    private final IssuePasswordResetCodeUseCase issuePasswordResetCodeUseCase;
    private final int resetCodeTtlMinutes;

    @Override
    @Transactional
    public ProvisionTeacherResult execute(ProvisionTeacherCommand command) {
        if (teacherRepository.existsByEmail(command.email())) {
            throw new DuplicateEmailException(command.email());
        }

        String firebaseUid = null;
        try {
            String displayName = command.firstName() + " " + command.lastName();
            firebaseUid = authPort.createUser(command.email(), displayName);

            Teacher teacher = Teacher.provision(
                firebaseUid, command.firstName(), command.lastName(),
                command.email(), AuthProvider.EMAIL_PASSWORD
            );
            teacherRepository.save(teacher);

            var codeResult = issuePasswordResetCodeUseCase.execute(
                IssuePasswordResetCodeCommand.builder()
                    .teacherUid(firebaseUid)
                    .ttlMinutes(resetCodeTtlMinutes)
                    .provider(SignInProvider.EMAIL_PASSWORD)
                    .build()
            );

            return ProvisionTeacherResult.builder()
                .firebaseUid(firebaseUid)
                .rawCode(codeResult.rawCode())
                .build();

        } catch (Exception ex) {
            if (firebaseUid != null) {
                try { authPort.deleteUser(firebaseUid); } catch (Exception ignored) {}
            }
            if (ex instanceof RuntimeException rte) throw rte;
            throw new RuntimeException(ex);
        }
    }
}
