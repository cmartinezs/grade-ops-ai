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
        AssessmentResult.AssessmentResultBuilder builder = AssessmentResult.builder()
                .title("Loop exercise")
                .context("Practice iteration")
                .instructions("Implement the requested program")
                .objectives(objectives)
                .deliverables(deliverables)
                .constraints(constraints);

        // when
        AssessmentResult result = builder.build();
        objectives.add("Mutated objective");
        deliverables.add("Mutated deliverable");
        constraints.add("Mutated constraint");

        // then — 1. no nulo
        assertThat(result).isNotNull();
        // then — 2. atributos no nulos
        assertThat(result.title()).isNotNull();
        assertThat(result.context()).isNotNull();
        assertThat(result.instructions()).isNotNull();
        assertThat(result.objectives()).isNotNull();
        assertThat(result.deliverables()).isNotNull();
        assertThat(result.constraints()).isNotNull();
        // then — 3. valores esperados, no afectados por la mutación posterior
        assertThat(result.title()).isEqualTo("Loop exercise");
        assertThat(result.context()).isEqualTo("Practice iteration");
        assertThat(result.instructions()).isEqualTo("Implement the requested program");
        assertThat(result.objectives()).containsExactly("Use for loops");
        assertThat(result.deliverables()).containsExactly("Source code");
        assertThat(result.constraints()).containsExactly("No external libraries");
    }

    @Test
    void shouldExposeUnmodifiableListFields() {
        // given
        AssessmentResult.AssessmentResultBuilder builder = AssessmentResult.builder()
                .title("Loop exercise")
                .context("Practice iteration")
                .instructions("Implement the requested program")
                .objectives(List.of("Use for loops"))
                .deliverables(List.of("Source code"))
                .constraints(List.of("No external libraries"));

        // when
        AssessmentResult result = builder.build();

        // then — 1. no nulo
        assertThat(result).isNotNull();
        // then — 2/3. valores esperados
        assertThat(result.title()).isEqualTo("Loop exercise");
        assertThat(result.context()).isEqualTo("Practice iteration");
        assertThat(result.instructions()).isEqualTo("Implement the requested program");
        assertThat(result.objectives()).containsExactly("Use for loops");
        assertThat(result.deliverables()).containsExactly("Source code");
        assertThat(result.constraints()).containsExactly("No external libraries");
        // then — 4. las listas expuestas son inmutables
        assertThatThrownBy(() -> result.objectives().add("Mutated objective"))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> result.deliverables().add("Mutated deliverable"))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> result.constraints().add("Mutated constraint"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldNormalizeNullListFieldToEmptyImmutableList() {
        // given
        AssessmentResult.AssessmentResultBuilder builder = AssessmentResult.builder()
                .title("Loop exercise")
                .context("Practice iteration")
                .instructions("Implement the requested program")
                .objectives(null)
                .deliverables(List.of("Source code"))
                .constraints(List.of("No external libraries"));

        // when
        AssessmentResult result = builder.build();

        // then — 1. no nulo
        assertThat(result).isNotNull();
        // then — 2. atributos no nulos, incluido objectives (normalizado, no null)
        assertThat(result.title()).isNotNull();
        assertThat(result.context()).isNotNull();
        assertThat(result.instructions()).isNotNull();
        assertThat(result.objectives()).isNotNull();
        // then — 3. valores esperados
        assertThat(result.title()).isEqualTo("Loop exercise");
        assertThat(result.context()).isEqualTo("Practice iteration");
        assertThat(result.instructions()).isEqualTo("Implement the requested program");
        assertThat(result.objectives()).isEmpty();
        assertThat(result.deliverables()).containsExactly("Source code");
        assertThat(result.constraints()).containsExactly("No external libraries");
        // then — 4. la lista normalizada también es inmutable
        assertThatThrownBy(() -> result.objectives().add("Mutated objective"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
