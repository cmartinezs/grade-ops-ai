package cl.gradeops.ai.agents.assessment.application.port.out;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cl.gradeops.ai.agents.assessment.application.port.out.AssessmentGenerationPortSelector.SelectedProvider;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AssessmentGenerationPortSelectorTest {

    private static final AssessmentGenerationPort GEMINI_PORT = (renderedPrompt, model) -> null;
    private static final AssessmentGenerationPort GROQ_PORT = (renderedPrompt, model) -> null;

    private final AssessmentGenerationPortSelector selector = new AssessmentGenerationPortSelector(
            Map.of("gemini", GEMINI_PORT, "groq", GROQ_PORT), "groq");

    @Test
    void shouldResolveTheNamedProviderWhenRequested() {
        // when
        SelectedProvider selected = selector.resolve("gemini");

        // then — 1. no nulo
        assertThat(selected).isNotNull();
        // then — 2. atributos no nulos
        assertThat(selected.name()).isNotNull();
        assertThat(selected.port()).isNotNull();
        // then — 3. valores esperados
        assertThat(selected.name()).isEqualTo("gemini");
        assertThat(selected.port()).isSameAs(GEMINI_PORT);
    }

    @Test
    void shouldFallBackToTheConfiguredDefaultProviderWhenRequestedProviderIsNull() {
        // when
        SelectedProvider selected = selector.resolve(null);

        // then — 1. no nulo
        assertThat(selected).isNotNull();
        // then — 2. atributos no nulos
        assertThat(selected.name()).isNotNull();
        assertThat(selected.port()).isNotNull();
        // then — 3. valores esperados — resolves to the configured default (groq), not gemini
        assertThat(selected.name()).isEqualTo("groq");
        assertThat(selected.port()).isSameAs(GROQ_PORT);
    }

    @Test
    void shouldTreatABlankProviderAsUnrecognizedRatherThanFallingBackToDefault() {
        // given — pins down actual behavior explicitly: only a literal null argument triggers
        // the default fallback. An empty string is looked up as a literal (unregistered)
        // provider name and rejected the same as any other unknown value, not treated as
        // equivalent to omitting the field.
        assertThat(selector.supports("")).isFalse();
        assertThatThrownBy(() -> selector.resolve(""))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No AssessmentGenerationPort registered for provider:");
    }

    @Test
    void shouldThrowAClearExceptionWhenResolvingAnUnrecognizedProvider() {
        // when / then
        assertThatThrownBy(() -> selector.resolve("bogus"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("bogus");
    }

    @Test
    void shouldReportNullAsSupportedSinceItResolvesToTheDefault() {
        // then
        assertThat(selector.supports(null)).isTrue();
    }

    @Test
    void shouldReportEveryRegisteredProviderNameAsSupported() {
        // then
        assertThat(selector.supports("gemini")).isTrue();
        assertThat(selector.supports("groq")).isTrue();
    }

    @Test
    void shouldReportAnUnrecognizedProviderNameAsNotSupported() {
        // then
        assertThat(selector.supports("bogus")).isFalse();
    }
}
