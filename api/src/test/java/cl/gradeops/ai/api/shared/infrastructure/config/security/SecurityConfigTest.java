package cl.gradeops.ai.api.shared.infrastructure.config.security;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

    private SecurityConfig config() {
        SecurityConfig config = new SecurityConfig();
        ReflectionTestUtils.setField(config, "allowedOrigins", "http://localhost:3000");
        return config;
    }

    @Test
    void shouldAllowPatchForDraftEditSaves() {
        CorsConfigurationSource source = config().corsConfigurationSource();
        CorsConfiguration resolved = ((UrlBasedCorsConfigurationSource) source)
                .getCorsConfigurations()
                .get("/**");

        assertThat(resolved.getAllowedMethods()).contains("PATCH");
    }
}
