package cl.gradeops.ai.api.shared.infrastructure.config;

import cl.gradeops.ai.api.shared.infrastructure.config.GradeOpsEmailProperties;
import cl.gradeops.ai.api.shared.infrastructure.config.GradeOpsWebProperties;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableConfigurationProperties({GradeOpsEmailProperties.class, GradeOpsWebProperties.class})
public class FirebaseConfig {

    @Value("${firebase.credentials-path:}")
    private String credentialsPath;

    @Value("${firebase.credentials-json:}")
    private String credentialsJson;

    @Bean
    @ConditionalOnMissingBean(FirebaseAuth.class)
    public FirebaseApp firebaseApp() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        GoogleCredentials credentials;
        if (credentialsJson != null && !credentialsJson.isBlank()) {
            credentials = GoogleCredentials.fromStream(
                    new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8)));
        } else if (credentialsPath != null && !credentialsPath.isBlank()) {
            try (var in = new FileInputStream(credentialsPath)) {
                credentials = GoogleCredentials.fromStream(in);
            }
        } else {
            credentials = GoogleCredentials.getApplicationDefault();
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build();
        return FirebaseApp.initializeApp(options);
    }

    @Bean
    @ConditionalOnMissingBean
    public FirebaseAuth firebaseAuth(FirebaseApp firebaseApp) {
        return FirebaseAuth.getInstance(firebaseApp);
    }
}
