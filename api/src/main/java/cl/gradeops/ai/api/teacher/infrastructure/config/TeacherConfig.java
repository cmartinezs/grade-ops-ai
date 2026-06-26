package cl.gradeops.ai.api.teacher.infrastructure.config;

import cl.gradeops.ai.api.auth.application.port.in.IssuePasswordResetCodeUseCase;
import cl.gradeops.ai.api.auth.application.port.out.AuthPort;
import cl.gradeops.ai.api.shared.infrastructure.config.GradeOpsEmailProperties;
import cl.gradeops.ai.api.teacher.application.port.out.TeacherRepositoryPort;
import cl.gradeops.ai.api.teacher.application.usecase.ProvisionTeacherHandler;
import cl.gradeops.ai.api.teacher.application.usecase.UpdatePilotFlagsHandler;
import cl.gradeops.ai.api.teacher.infrastructure.adapter.out.persistence.TeacherJpaRepository;
import cl.gradeops.ai.api.teacher.infrastructure.adapter.out.persistence.TeacherPersistenceAdapter;
import cl.gradeops.ai.api.teacher.infrastructure.adapter.out.persistence.TeacherPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
class TeacherConfig {

    private final GradeOpsEmailProperties emailProperties;

    @Bean
    TeacherPersistenceMapper teacherPersistenceMapper() {
        return new TeacherPersistenceMapper();
    }

    @Bean
    TeacherPersistenceAdapter teacherPersistenceAdapter(
            TeacherJpaRepository jpaRepository,
            TeacherPersistenceMapper mapper) {
        return new TeacherPersistenceAdapter(jpaRepository, mapper);
    }

    @Bean
    ProvisionTeacherHandler provisionTeacherHandler(
            AuthPort authPort,
            TeacherRepositoryPort teacherRepository,
            IssuePasswordResetCodeUseCase issuePasswordResetCodeUseCase) {
        int ttlMinutes = emailProperties.getResetPassword().getTtlMinutes();
        return new ProvisionTeacherHandler(authPort, teacherRepository, issuePasswordResetCodeUseCase, ttlMinutes);
    }

    @Bean
    UpdatePilotFlagsHandler updatePilotFlagsHandler(TeacherRepositoryPort teacherRepository) {
        return new UpdatePilotFlagsHandler(teacherRepository);
    }
}
