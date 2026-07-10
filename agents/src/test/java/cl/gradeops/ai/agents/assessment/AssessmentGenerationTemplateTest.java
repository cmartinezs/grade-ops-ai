package cl.gradeops.ai.agents.assessment;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.stringtemplate.v4.ST;

class AssessmentGenerationTemplateTest {

    private static final String RESOURCE_PATH = "prompts/assessment-generation.st";

    private String headerComment;
    private String templateBody;

    @BeforeEach
    void loadTemplate() throws IOException {
        // given
        String raw;
        try (InputStream in = AssessmentGenerationTemplateTest.class
                .getClassLoader()
                .getResourceAsStream(RESOURCE_PATH)) {
            raw = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        // when
        int firstNewline = raw.indexOf('\n');

        // then — split the version header from the renderable body once for all tests
        headerComment = raw.substring(0, firstNewline).strip();
        templateBody = raw.substring(firstNewline + 1);
    }

    @Test
    void shouldHaveHeaderCommentMatchingTheDocumentedVersionFormat() {
        // then
        assertThat(headerComment).isNotNull();
        assertThat(headerComment).isEqualTo("// assessment-generation.v1");
    }

    @Test
    void shouldRenderInitialGenerationCaseWithoutAdjustmentBranch() {
        // given
        ST template = new ST(templateBody);
        template.add("learningGoal", "Evaluate whether students can implement iterative algorithms correctly");
        template.add("topic", "Array manipulation and loops");
        template.add("level", "introductory");
        template.add("duration", "60 minutes");
        template.add("language", "Python");
        // AssessmentAgentOrchestrator (task-03) always calls add() with AssessmentCommand's
        // (possibly-null) adjustmentNotes/previousDraft — mirror that instead of omitting the
        // attribute, which would trigger ST4's "attribute isn't defined" warning for no reason.
        template.add("adjustmentNotes", null);
        template.add("previousDraft", null);

        // when
        String rendered = template.render();

        // then — 1. no nulo / no blank
        assertThat(rendered).isNotNull();
        assertThat(rendered).isNotBlank();
        // then — 2. atributos requeridos presentes en el texto renderizado
        assertThat(rendered).contains("Array manipulation and loops");
        assertThat(rendered).contains("introductory");
        assertThat(rendered).contains("60 minutes");
        assertThat(rendered).contains("Python");
        // then — 3. la rama de regeneración no aparece cuando adjustmentNotes no fue provisto
        assertThat(rendered).doesNotContain("Requested adjustments");
        assertThat(rendered).doesNotContain("Previous draft");
    }

    @Test
    void shouldRenderRegenerationCaseWithAdjustmentBranch() {
        // given
        ST template = new ST(templateBody);
        template.add("learningGoal", "Evaluate whether students can implement iterative algorithms correctly");
        template.add("topic", "Array manipulation and loops");
        template.add("level", "introductory");
        template.add("duration", "60 minutes");
        template.add("language", "Python");
        template.add("adjustmentNotes", "Make it shorter, students only have 40 minutes now.");
        template.add("previousDraft", "Title: Array Rotation Practice. Constraints: 60 minute time limit.");

        // when
        String rendered = template.render();

        // then — 1. no nulo / no blank
        assertThat(rendered).isNotNull();
        assertThat(rendered).isNotBlank();
        // then — 2. atributos requeridos presentes
        assertThat(rendered).contains("Array manipulation and loops");
        assertThat(rendered).contains("introductory");
        // then — 3. la rama de regeneración se activa con su contenido exacto
        assertThat(rendered).contains("Requested adjustments");
        assertThat(rendered).contains("Make it shorter, students only have 40 minutes now.");
        assertThat(rendered).contains("Previous draft");
        assertThat(rendered).contains("Title: Array Rotation Practice. Constraints: 60 minute time limit.");
    }
}
