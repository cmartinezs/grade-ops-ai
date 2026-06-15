package cl.gradeops.ai.api.config;

import com.google.firebase.auth.FirebaseAuth;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class FirebaseTestConfig {

    @Bean
    @Primary
    public FirebaseAuth firebaseAuth() {
        return mock(FirebaseAuth.class);
    }
}
