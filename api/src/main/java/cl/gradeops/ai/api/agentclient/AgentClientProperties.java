package cl.gradeops.ai.api.agentclient;

import java.time.Duration;

/**
 * A plain, framework-free holder for {@code agentclient} tuning values that the application
 * layer legitimately needs to read (e.g. the {@code INDETERMINATE} threshold computation in
 * {@code GetGenerationStatusHandler}/{@code RetryGenerationHandler}, per LOCAL-CONTRACTS.md §
 * "Orphaned in-flight detection": {@code now() - dispatchedAt > 5 × agentclient.read-timeout}).
 * {@link AgentClientConfig} (the {@code @Configuration} class building the actual {@code
 * RestClient}) is the single source of truth and reads from here too — this class holds the
 * value, not a second copy of it.
 */
public final class AgentClientProperties {

    public static final Duration READ_TIMEOUT = Duration.ofSeconds(60);

    private AgentClientProperties() {}
}
