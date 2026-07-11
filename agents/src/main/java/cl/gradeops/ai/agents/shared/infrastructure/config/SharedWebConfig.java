package cl.gradeops.ai.agents.shared.infrastructure.config;

import cl.gradeops.ai.agents.shared.infrastructure.adapter.in.web.CorrelationIdFilter;
import cl.gradeops.ai.agents.shared.infrastructure.adapter.in.web.InternalAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the filters every current and future {@code agents/} endpoint needs — not
 * feature-scoped, so this stays outside {@code AssessmentConfig}. Order matters: correlation ID
 * must be established before the auth check runs, so a rejected request still gets one.
 */
@Configuration
class SharedWebConfig {

    @Value("${app.internal.secret}")
    private String internalSecret;

    @Bean
    FilterRegistrationBean<CorrelationIdFilter> correlationIdFilterRegistration() {
        FilterRegistrationBean<CorrelationIdFilter> registration =
                new FilterRegistrationBean<>(new CorrelationIdFilter());
        registration.setOrder(1);
        return registration;
    }

    @Bean
    FilterRegistrationBean<InternalAuthFilter> internalAuthFilterRegistration() {
        FilterRegistrationBean<InternalAuthFilter> registration =
                new FilterRegistrationBean<>(new InternalAuthFilter(internalSecret));
        registration.setOrder(2);
        return registration;
    }
}
