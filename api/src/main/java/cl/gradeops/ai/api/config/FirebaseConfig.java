package cl.gradeops.ai.api.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.credentials-path:}")
    private String credentialsPath;

    @Bean
    @ConditionalOnMissingBean(FirebaseAuth.class)
    public FirebaseApp firebaseApp() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        GoogleCredentials credentials;
        if (credentialsPath != null && !credentialsPath.isBlank()) {
            try (var in = new FileInputStream(credentialsPath)) {
                credentials = GoogleCredentials.fromStream(in);
            }
        } else {
            // Production: ADC (Cloud Run service account, GOOGLE_APPLICATION_CREDENTIALS, etc.)
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
