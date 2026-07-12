package cl.gradeops.ai.agents.shared.infrastructure.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Loads a local {@code .env} file (project root, `KEY=VALUE` per line) into the Spring
 * {@code Environment} as the lowest-precedence property source — a real shell-exported
 * variable or a {@code -D} system property always wins over a `.env` value. This exists purely
 * for local developer convenience (no `export` needed before running the app); it is not a
 * general override mechanism and never touches deployed environments, which set their own
 * variables directly (Cloud Run env vars, Secret Manager).
 *
 * <p>Registered via {@code META-INF/spring.factories} rather than the newer
 * {@code AutoConfiguration.imports} file — an {@code EnvironmentPostProcessor} must run before
 * the {@code ApplicationContext} exists, earlier than the stage the `.imports` mechanism
 * applies to.
 */
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String DOTENV_FILE_NAME = ".env";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Path dotenvPath = Path.of(DOTENV_FILE_NAME);
        if (!Files.isRegularFile(dotenvPath)) {
            return;
        }

        Map<String, Object> values = new LinkedHashMap<>();
        List<String> lines;
        try {
            lines = Files.readAllLines(dotenvPath);
        } catch (IOException e) {
            return;
        }

        for (String rawLine : lines) {
            String line = rawLine.strip();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            int separatorIndex = line.indexOf('=');
            if (separatorIndex <= 0) {
                continue;
            }
            String key = line.substring(0, separatorIndex).strip();
            String value = line.substring(separatorIndex + 1).strip();
            if (value.length() >= 2
                    && ((value.startsWith("\"") && value.endsWith("\""))
                            || (value.startsWith("'") && value.endsWith("'")))) {
                value = value.substring(1, value.length() - 1);
            }
            values.put(key, value);
        }

        if (!values.isEmpty()) {
            environment.getPropertySources().addLast(new MapPropertySource("dotenv", values));
        }
    }
}
