package cl.gradeops.ai.agents.shared.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * GradeOps AI standardizes on Spring Boot 4's auto-configured Jackson 3 {@code
 * tools.jackson.databind.json.JsonMapper} (see {@code docs/RULES.md} § "JSON serialization —
 * Spring Boot 4 / Jackson 3"). This test scans only {@code agents/}'s own maintained sources
 * (never dependency jars, never {@code target/}) so the legacy Jackson 2 {@code ObjectMapper}
 * cannot silently creep back in — e.g. by copy-pasting a snippet from documentation or another
 * project that predates the Jackson 3 migration.
 *
 * <p>This does not assert anything about transitive dependencies: third-party SDKs (the Google
 * GenAI client, the OpenAI-compatible client used for Groq, the Logstash encoder) legitimately
 * carry their own internal Jackson 2 usage that never touches {@code agents/}'s own code — see
 * {@code AGENTS-HANDOFF.md} § "Dependency inspection" for that verification.
 */
class JacksonUsageGuardTest {

    private static final List<Path> SOURCE_ROOTS = List.of(Path.of("src/main/java"), Path.of("src/test/java"));

    /** This test's own file necessarily contains the forbidden literals below, as data — exclude it. */
    private static final String SELF = "JacksonUsageGuardTest.java";

    private static final List<String> FORBIDDEN_PATTERNS = List.of(
            "com.fasterxml.jackson.databind.ObjectMapper",
            "new ObjectMapper(",
            "com.fasterxml.jackson.datatype.jsr310.JavaTimeModule",
            "new JavaTimeModule(");

    @Test
    void agentsSourceNeverUsesJackson2ObjectMapperOrManualJavaTimeModule() throws IOException {
        List<String> violations = new ArrayList<>();

        for (Path root : SOURCE_ROOTS) {
            if (!Files.isDirectory(root)) {
                continue;
            }
            try (Stream<Path> files = Files.walk(root)) {
                files.filter(p -> p.toString().endsWith(".java"))
                        .filter(p -> !p.getFileName().toString().equals(SELF))
                        .forEach(p -> scan(p, violations));
            }
        }

        assertThat(violations)
                .as("agents/ must use Spring Boot's auto-configured Jackson 3 JsonMapper "
                        + "(tools.jackson.databind.json.JsonMapper), never Jackson 2's "
                        + "ObjectMapper or a manually-registered JavaTimeModule")
                .isEmpty();
    }

    private static void scan(Path file, List<String> violations) {
        String content;
        try {
            content = Files.readString(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        for (String pattern : FORBIDDEN_PATTERNS) {
            if (content.contains(pattern)) {
                violations.add(file + " contains forbidden pattern: " + pattern);
            }
        }
    }
}
