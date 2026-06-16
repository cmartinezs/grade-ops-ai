package cl.gradeops.ai.api.adapter.storage;

import cl.gradeops.ai.api.port.StoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import jakarta.annotation.PreDestroy;
import java.net.URI;
import java.time.Duration;

@Component
@Profile("beta")
public class R2StorageAdapter implements StoragePort {

    private final S3Client s3;
    private final S3Presigner presigner;
    private final String bucket;

    public R2StorageAdapter(
            @Value("${r2.account-id}") String accountId,
            @Value("${r2.access-key}") String accessKey,
            @Value("${r2.secret-key}") String secretKey,
            @Value("${r2.bucket}") String bucket) {
        this.bucket = bucket;
        URI endpoint = URI.create("https://" + accountId + ".r2.cloudflarestorage.com");
        StaticCredentialsProvider creds = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey));
        this.s3 = S3Client.builder()
                .endpointOverride(endpoint)
                .region(Region.of("auto"))
                .credentialsProvider(creds)
                .build();
        this.presigner = S3Presigner.builder()
                .endpointOverride(endpoint)
                .region(Region.of("auto"))
                .credentialsProvider(creds)
                .build();
    }

    @Override
    public void store(String key, byte[] content, String contentType) {
        s3.putObject(PutObjectRequest.builder()
                        .bucket(bucket).key(key).contentType(contentType).build(),
                RequestBody.fromBytes(content));
    }

    @Override
    public byte[] retrieve(String key) {
        return s3.getObjectAsBytes(
                GetObjectRequest.builder().bucket(bucket).key(key).build()).asByteArray();
    }

    @Override
    public void delete(String key) {
        s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
    }

    @Override
    public URI signedUrl(String key, Duration ttl) {
        try {
            return presigner.presignGetObject(GetObjectPresignRequest.builder()
                            .signatureDuration(ttl)
                            .getObjectRequest(r -> r.bucket(bucket).key(key))
                            .build())
                    .url().toURI();
        } catch (java.net.URISyntaxException e) {
            throw new RuntimeException("Failed to build presigned URL", e);
        }
    }

    @PreDestroy
    public void close() {
        s3.close();
        presigner.close();
    }
}
