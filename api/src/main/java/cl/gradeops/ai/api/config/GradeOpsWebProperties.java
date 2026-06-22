package cl.gradeops.ai.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "gradeops.web")
public class GradeOpsWebProperties {

    private String baseUrl = "http://localhost:3000";

    public String getBaseUrl()       { return baseUrl; }
    public void setBaseUrl(String v) { this.baseUrl = v; }
}
