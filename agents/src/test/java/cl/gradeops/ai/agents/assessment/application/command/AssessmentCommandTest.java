package cl.gradeops.ai.agents.assessment.application.command;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AssessmentCommandTest {

    @Test
    void shouldCreateInitialGenerationCommandWhenRequiredFieldsArePresent() {
        // given
        AssessmentCommand command = AssessmentCommand.builder()
                .learningGoal("Evaluate loops")
                .topic("Java loops")
                .level("introductory")
                .duration("60 minutes")
                .language("Java")
                .build();

        // when
        String topic = command.topic();

        // then
        assertThat(topic).isEqualTo("Java loops");
        assertThat(command.adjustmentNotes()).isNull();
        assertThat(command.previousDraftId()).isNull();
    }

    @Test
    void shouldCreateRegenerationCommandWhenAdjustmentFieldsAreProvidedTogether() {
        // given
        AssessmentCommand command = AssessmentCommand.builder()
                .learningGoal("Evaluate loops")
                .topic("Java loops")
                .level("introductory")
                .duration("60 minutes")
                .language("Java")
                .adjustmentNotes("Make it shorter")
                .previousDraftId("draft-123")
                .build();

        // when
        String adjustmentNotes = command.adjustmentNotes();

        // then
        assertThat(adjustmentNotes).isEqualTo("Make it shorter");
        assertThat(command.previousDraftId()).isEqualTo("draft-123");
    }

    @Test
    void shouldConstructCommandWithoutValidatingRequiredFieldsOrAdjustmentPairing() {
        // given / when — a null required field and a mismatched adjustment pairing are both
        // malformed-input concerns for AssessmentAgentService.validate (task-03), not this record.
        AssessmentCommand command = AssessmentCommand.builder()
                .learningGoal(null)
                .topic("Java loops")
                .level("introductory")
                .duration("60 minutes")
                .language("Java")
                .adjustmentNotes("Make it shorter")
                .build();

        // then
        assertThat(command.learningGoal()).isNull();
        assertThat(command.adjustmentNotes()).isEqualTo("Make it shorter");
        assertThat(command.previousDraftId()).isNull();
    }
}
