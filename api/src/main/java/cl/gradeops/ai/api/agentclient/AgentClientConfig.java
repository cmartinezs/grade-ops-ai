package cl.gradeops.ai.api.agentclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
class AgentClientConfig {

    // Generation calls reach an LLM provider, which can be slow; connect stays short since a
    // hung TCP handshake means agents/ is unreachable, not busy.
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(60);

    @Bean
    RestClient agentsRestClient(
            @Value("${app.agents.base-url}") String agentsBaseUrl,
            @Value("${app.internal.secret}") String internalSecret) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
        requestFactory.setReadTimeout(READ_TIMEOUT);

        return RestClient.builder()
                .baseUrl(agentsBaseUrl)
                .requestFactory(requestFactory)
                .defaultHeader("X-Internal-Key", internalSecret)
                .build();
    }

    @Bean
    AssessmentAgentClient assessmentAgentClient(RestClient agentsRestClient) {
        return new AssessmentAgentClient(agentsRestClient);
    }
}
