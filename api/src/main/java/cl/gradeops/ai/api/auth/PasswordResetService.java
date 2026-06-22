package cl.gradeops.ai.api.auth;

import cl.gradeops.ai.api.config.GradeOpsEmailProperties;
import cl.gradeops.ai.api.config.GradeOpsWebProperties;
import cl.gradeops.ai.api.domain.teacher.TeacherEntity;
import cl.gradeops.ai.api.domain.teacher.TeacherRepository;
import cl.gradeops.ai.api.email.EmailService;
import cl.gradeops.ai.api.port.AuthPort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final TeacherRepository teacherRepository;
    private final PasswordResetCodeRepository codeRepository;
    private final EmailService emailService;
    private final AuthPort authPort;
    private final GradeOpsEmailProperties emailProperties;
    private final GradeOpsWebProperties webProperties;

    public PasswordResetService(TeacherRepository teacherRepository,
                                PasswordResetCodeRepository codeRepository,
                                EmailService emailService,
                                AuthPort authPort,
                                GradeOpsEmailProperties emailProperties,
                                GradeOpsWebProperties webProperties) {
        this.teacherRepository = teacherRepository;
        this.codeRepository = codeRepository;
        this.emailService = emailService;
        this.authPort = authPort;
        this.emailProperties = emailProperties;
        this.webProperties = webProperties;
    }

    @Transactional
    public void sendResetEmail(ForgotPasswordRequest request) {
        Optional<TeacherEntity> maybeTeacher = teacherRepository.findByEmail(request.email());
        if (maybeTeacher.isEmpty()) return;

        TeacherEntity teacher = maybeTeacher.get();
        if (!"EMAIL_PASSWORD".equals(teacher.getProvider())) return;

        String rawCode = UUID.randomUUID().toString();
        int ttlMinutes = emailProperties.getResetPassword().getTtlMinutes();
        Instant expiresAt = Instant.now().plus(ttlMinutes, ChronoUnit.MINUTES);

        codeRepository.deleteByTeacherUid(teacher.getFirebaseUid());
        codeRepository.save(new PasswordResetCodeEntity(teacher.getFirebaseUid(), rawCode, expiresAt));

        String resetLink = webProperties.getBaseUrl() + "/reset-password?code=" + rawCode;
        emailService.sendPasswordReset(teacher.getEmail(), teacher.getFirstName(), resetLink);
    }

    @Transactional
    public void resetPassword(String code, ResetPasswordRequest request) {
        PasswordResetCodeEntity resetCode = codeRepository.findByCode(code)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "RESET_CODE_NOT_FOUND"));

        if (resetCode.isExpired()) {
            throw new ResponseStatusException(HttpStatus.GONE, "RESET_CODE_EXPIRED");
        }
        if (resetCode.isUsed()) {
            throw new ResponseStatusException(HttpStatus.GONE, "RESET_CODE_USED");
        }

        TeacherEntity teacher = teacherRepository.findById(resetCode.getTeacherUid())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "RESET_CODE_NOT_FOUND"));

        if (!teacher.getEmail().equalsIgnoreCase(request.email())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "RESET_CODE_EMAIL_MISMATCH");
        }

        authPort.updatePassword(teacher.getFirebaseUid(), request.password());
        resetCode.markUsed();
        codeRepository.save(resetCode);
    }
}
