package cl.gradeops.ai.agents.assessment.application.port.out;

import java.util.Map;

/**
 * Strategy-pattern resolver over every registered {@link AssessmentGenerationPort}. {@code
 * portsByProvider} is populated by Spring's own {@code Map<String, T>} autowiring — each key is
 * the bean name given to a provider's adapter in {@code AssessmentConfig} (e.g. {@code "gemini"},
 * {@code "groq"}). Adding a future provider only means registering one more named {@code @Bean}
 * there; this class never changes.
 *
 * <p>{@link #supports(String)} lets {@code AssessmentAgentOrchestrator.validate} reject an
 * unrecognized {@code provider} value up front, before generation is attempted, with the
 * project's own {@code AssessmentAgentException(INVALID_COMMAND)} rather than letting {@link
 * #resolve(String)} fail with a generic exception mid-pipeline.
 */
public class AssessmentGenerationPortSelector {

    private final Map<String, AssessmentGenerationPort> portsByProvider;
    private final String defaultProvider;

    public AssessmentGenerationPortSelector(
            Map<String, AssessmentGenerationPort> portsByProvider, String defaultProvider) {
        this.portsByProvider = portsByProvider;
        this.defaultProvider = defaultProvider;
    }

    /**
     * @param provider a candidate {@code AssessmentCommand.provider} value; {@code null} is
     *     always supported since it resolves to the configured default
     */
    public boolean supports(String provider) {
        return provider == null || portsByProvider.containsKey(provider);
    }

    /**
     * @param requestedProvider {@code AssessmentCommand.provider}, or {@code null} to use the
     *     configured default
     * @throws IllegalStateException if {@code requestedProvider} (or the configured default) has
     *     no registered adapter — callers are expected to have already checked {@link
     *     #supports(String)} during command validation, so this is a defensive fallback, not the
     *     primary error path for an unrecognized provider
     */
    public SelectedProvider resolve(String requestedProvider) {
        String name = requestedProvider != null ? requestedProvider : defaultProvider;
        AssessmentGenerationPort port = portsByProvider.get(name);
        if (port == null) {
            throw new IllegalStateException("No AssessmentGenerationPort registered for provider: " + name);
        }
        return new SelectedProvider(name, port);
    }

    /** The provider name a request resolved to, paired with the port that will handle it. */
    public record SelectedProvider(String name, AssessmentGenerationPort port) {
    }
}
