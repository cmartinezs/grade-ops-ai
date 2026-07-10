package cl.gradeops.ai.agents.assessment.application.result;

import static org.assertj.core.api.Assertions.assertThat;
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
        AssessmentResult result = AssessmentResult.builder()
                .title("Loop exercise")
                .context("Practice iteration")
                .instructions("Implement the requested program")
                .objectives(objectives)
                .deliverables(deliverables)
                .constraints(constraints)
                .build();

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
        AssessmentResult result = AssessmentResult.builder()
                .title("Loop exercise")
                .context("Practice iteration")
                .instructions("Implement the requested program")
                .objectives(List.of("Use for loops"))
                .deliverables(List.of("Source code"))
                .constraints(List.of("No external libraries"))
                .build();

        // when / then
        assertThatThrownBy(() -> result.objectives().add("Mutated objective"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldNormalizeNullListFieldToEmptyImmutableList() {
        // given
        AssessmentResult result = AssessmentResult.builder()
                .title("Loop exercise")
                .context("Practice iteration")
                .instructions("Implement the requested program")
                .objectives(null)
                .deliverables(List.of("Source code"))
                .constraints(List.of("No external libraries"))
                .build();

        // then
        assertThat(result.objectives()).isEmpty();
        assertThatThrownBy(() -> result.objectives().add("Mutated objective"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
