package cl.gradeops.ai.agents.assessment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class AssessmentCommandTest {

    @Test
    void shouldCreateInitialGenerationCommandWhenRequiredFieldsArePresent() {
        // given
        AssessmentCommand command = new AssessmentCommand(
                "Evaluate loops",
                "Java loops",
                "introductory",
                "60 minutes",
                "Java",
                null,
                null);

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
        AssessmentCommand command = new AssessmentCommand(
                "Evaluate loops",
                "Java loops",
                "introductory",
                "60 minutes",
                "Java",
                "Make it shorter",
                "draft-123");

        // when
        String adjustmentNotes = command.adjustmentNotes();

        // then
        assertThat(adjustmentNotes).isEqualTo("Make it shorter");
        assertThat(command.previousDraftId()).isEqualTo("draft-123");
    }

    @Test
    void shouldRejectCommandWhenRequiredFieldIsNull() {
        // given
        String learningGoal = null;

        // when / then
        assertThatNullPointerException()
                .isThrownBy(() -> new AssessmentCommand(
                        learningGoal,
                        "Java loops",
                        "introductory",
                        "60 minutes",
                        "Java",
                        null,
                        null))
                .withMessage("learningGoal is required");
    }

    @Test
    void shouldRejectRegenerationCommandWhenOnlyAdjustmentNotesAreProvided() {
        // given
        String adjustmentNotes = "Make it shorter";

        // when / then
        assertThatThrownBy(() -> new AssessmentCommand(
                        "Evaluate loops",
                        "Java loops",
                        "introductory",
                        "60 minutes",
                        "Java",
                        adjustmentNotes,
                        null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("adjustmentNotes and previousDraftId must be provided together for regeneration");
    }
}
