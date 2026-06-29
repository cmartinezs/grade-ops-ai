package cl.gradeops.ai.api.shared.infrastructure.config;

import cl.gradeops.ai.api.shared.infrastructure.adapter.out.storage.GcsStorageAdapter;
import cl.gradeops.ai.api.shared.infrastructure.adapter.out.storage.R2StorageAdapter;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
class StorageConfig {

    @Bean
    @Profile("beta")
    R2StorageAdapter r2StorageAdapter(
            @Value("${r2.account-id}") String accountId,
            @Value("${r2.access-key}") String accessKey,
            @Value("${r2.secret-key}") String secretKey,
            @Value("${r2.bucket}") String bucket) {
        return new R2StorageAdapter(accountId, accessKey, secretKey, bucket);
    }

    @Bean
    @Profile("demo")
    GcsStorageAdapter gcsStorageAdapter(
            Storage storage,
            @Value("${gcs.bucket}") String bucket) {
        return new GcsStorageAdapter(storage, bucket);
    }
}
