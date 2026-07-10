package cl.gradeops.ai.agents.assessment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class AssessmentResultTest {

    @Test
    void shouldDefensivelyCopyListFields() {
        // given
        List<String> objectives = new ArrayList<>(List.of("Use for loops"));
        List<String> deliverables = new ArrayList<>(List.of("Source code"));
        List<String> constraints = new ArrayList<>(List.of("No external libraries"));
        AssessmentResult result = new AssessmentResult(
                "Loop exercise",
                "Practice iteration",
                "Implement the requested program",
                objectives,
                deliverables,
                constraints);

        // when
        objectives.add("Mutated objective");
        deliverables.add("Mutated deliverable");
        constraints.add("Mutated constraint");

        // then
        assertThat(result.objectives()).containsExactly("Use for loops");
        assertThat(result.deliverables()).containsExactly("Source code");
        assertThat(result.constraints()).containsExactly("No external libraries");
    }

    @Test
    void shouldExposeUnmodifiableListFields() {
        // given
        AssessmentResult result = new AssessmentResult(
                "Loop exercise",
                "Practice iteration",
                "Implement the requested program",
                List.of("Use for loops"),
                List.of("Source code"),
                List.of("No external libraries"));

        // when / then
        assertThatThrownBy(() -> result.objectives().add("Mutated objective"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldRejectResultWhenRequiredTextFieldIsNull() {
        // given
        String title = null;

        // when / then
        assertThatNullPointerException()
                .isThrownBy(() -> new AssessmentResult(
                        title,
                        "Practice iteration",
                        "Implement the requested program",
                        List.of("Use for loops"),
                        List.of("Source code"),
                        List.of("No external libraries")))
                .withMessage("title is required");
    }

    @Test
    void shouldRejectResultWhenRequiredListFieldIsNull() {
        // given
        List<String> objectives = null;

        // when / then
        assertThatNullPointerException()
                .isThrownBy(() -> new AssessmentResult(
                        "Loop exercise",
                        "Practice iteration",
                        "Implement the requested program",
                        objectives,
                        List.of("Source code"),
                        List.of("No external libraries")))
                .withMessage("objectives is required");
    }
}
