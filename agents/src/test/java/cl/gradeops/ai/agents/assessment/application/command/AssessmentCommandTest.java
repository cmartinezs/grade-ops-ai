package cl.gradeops.ai.agents.assessment.application.command;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AssessmentCommandTest {

    @Test
    void shouldCreateInitialGenerationCommandWhenRequiredFieldsArePresent() {
        // given
        AssessmentCommand.AssessmentCommandBuilder builder = AssessmentCommand.builder()
                .learningGoal("Evaluate loops")
                .topic("Java loops")
                .level("introductory")
                .duration("60 minutes")
                .language("Java");

        // when
        AssessmentCommand command = builder.build();

        // then — 1. no nulo
        assertThat(command).isNotNull();
        // then — 2. atributos requeridos no nulos
        assertThat(command.learningGoal()).isNotNull();
        assertThat(command.topic()).isNotNull();
        assertThat(command.level()).isNotNull();
        assertThat(command.duration()).isNotNull();
        assertThat(command.language()).isNotNull();
        // then — 3. valores esperados
        assertThat(command.learningGoal()).isEqualTo("Evaluate loops");
        assertThat(command.topic()).isEqualTo("Java loops");
        assertThat(command.level()).isEqualTo("introductory");
        assertThat(command.duration()).isEqualTo("60 minutes");
        assertThat(command.language()).isEqualTo("Java");
        // then — 4. campos de regeneración ausentes en generación inicial
        assertThat(command.adjustmentNotes()).isNull();
        assertThat(command.previousDraftId()).isNull();
    }

    @Test
    void shouldCreateRegenerationCommandWhenAdjustmentFieldsAreProvidedTogether() {
        // given
        AssessmentCommand.AssessmentCommandBuilder builder = AssessmentCommand.builder()
                .learningGoal("Evaluate loops")
                .topic("Java loops")
                .level("introductory")
                .duration("60 minutes")
                .language("Java")
                .adjustmentNotes("Make it shorter")
                .previousDraftId("draft-123");

        // when
        AssessmentCommand command = builder.build();

        // then — 1. no nulo
        assertThat(command).isNotNull();
        // then — 2. atributos no nulos
        assertThat(command.learningGoal()).isNotNull();
        assertThat(command.topic()).isNotNull();
        assertThat(command.level()).isNotNull();
        assertThat(command.duration()).isNotNull();
        assertThat(command.language()).isNotNull();
        assertThat(command.adjustmentNotes()).isNotNull();
        assertThat(command.previousDraftId()).isNotNull();
        // then — 3. valores esperados
        assertThat(command.learningGoal()).isEqualTo("Evaluate loops");
        assertThat(command.topic()).isEqualTo("Java loops");
        assertThat(command.level()).isEqualTo("introductory");
        assertThat(command.duration()).isEqualTo("60 minutes");
        assertThat(command.language()).isEqualTo("Java");
        assertThat(command.adjustmentNotes()).isEqualTo("Make it shorter");
        assertThat(command.previousDraftId()).isEqualTo("draft-123");
    }

    @Test
    void shouldConstructCommandWithoutValidatingRequiredFieldsOrAdjustmentPairing() {
        // given — a null required field and a mismatched adjustment pairing are both
        // malformed-input concerns for AssessmentAgentService.validate (task-03), not this record.
        AssessmentCommand.AssessmentCommandBuilder builder = AssessmentCommand.builder()
                .learningGoal(null)
                .topic("Java loops")
                .level("introductory")
                .duration("60 minutes")
                .language("Java")
                .adjustmentNotes("Make it shorter");

        // when
        AssessmentCommand command = builder.build();

        // then — 1. no nulo
        assertThat(command).isNotNull();
        // then — 2/3. valores esperados, incluyendo los nulos intencionales
        assertThat(command.learningGoal()).isNull();
        assertThat(command.topic()).isEqualTo("Java loops");
        assertThat(command.level()).isEqualTo("introductory");
        assertThat(command.duration()).isEqualTo("60 minutes");
        assertThat(command.language()).isEqualTo("Java");
        assertThat(command.adjustmentNotes()).isEqualTo("Make it shorter");
        assertThat(command.previousDraftId()).isNull();
    }
}
