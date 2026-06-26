package cl.gradeops.ai.api.teacher.application.usecase;

import cl.gradeops.ai.api.teacher.application.command.UpdatePilotFlagsCommand;
import cl.gradeops.ai.api.teacher.application.port.out.TeacherRepositoryPort;
import cl.gradeops.ai.api.teacher.application.result.UpdatePilotFlagsResult;
import cl.gradeops.ai.api.teacher.domain.exception.TeacherNotFoundException;
import cl.gradeops.ai.api.teacher.domain.model.AuthProvider;
import cl.gradeops.ai.api.teacher.domain.model.Teacher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdatePilotFlagsHandlerTest {

    @Mock TeacherRepositoryPort teacherRepository;

    UpdatePilotFlagsHandler handler() {
        return new UpdatePilotFlagsHandler(teacherRepository);
    }

    @Test
    void shouldThrowTeacherNotFoundExceptionWhenTeacherDoesNotExist() {
        when(teacherRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler().execute(
                UpdatePilotFlagsCommand.builder().uid("unknown").build()))
                .isInstanceOf(TeacherNotFoundException.class);

        verify(teacherRepository, never()).save(any());
    }

    @Test
    void shouldSaveAndReturnUpdatedResultWhenTeacherExists() {
        Teacher teacher = Teacher.restore("uid-1", "Ana", "Soto", "a@x.com",
                AuthProvider.EMAIL_PASSWORD, OffsetDateTime.now(), OffsetDateTime.now(),
                null, false, null, null, null, null);
        when(teacherRepository.findById("uid-1")).thenReturn(Optional.of(teacher));

        UpdatePilotFlagsResult result = handler().execute(
                UpdatePilotFlagsCommand.builder()
                    .uid("uid-1").planType("pilot").relatedParty(true).build());

        verify(teacherRepository).save(teacher);
        assertThat(result.planType()).isEqualTo("pilot");
        assertThat(result.relatedParty()).isTrue();
        assertThat(result.flagSetAt()).isNotNull();
    }

    @Test
    void shouldPreserveRelatedPartyWhenNullValueProvided() {
        Teacher teacher = Teacher.restore("uid-2", "X", "Y", "x@y.com",
                AuthProvider.EMAIL_PASSWORD, OffsetDateTime.now(), OffsetDateTime.now(),
                null, true, null, null, null, null);
        when(teacherRepository.findById("uid-2")).thenReturn(Optional.of(teacher));

        handler().execute(UpdatePilotFlagsCommand.builder()
                .uid("uid-2").planType("pilot").relatedParty(null).build());

        assertThat(teacher.isRelatedParty()).isTrue();
    }
}
