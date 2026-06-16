package cl.gradeops.ai.api.adapter.storage;

import cl.gradeops.ai.api.port.StoragePort;
import com.google.cloud.storage.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
@Profile("demo")
public class GcsStorageAdapter implements StoragePort {

    private final Storage storage;
    private final String bucket;

    public GcsStorageAdapter(Storage storage, @Value("${gcs.bucket}") String bucket) {
        this.storage = storage;
        this.bucket = bucket;
    }

    @Override
    public void store(String key, byte[] content, String contentType) {
        BlobInfo info = BlobInfo.newBuilder(BlobId.of(bucket, key))
                .setContentType(contentType).build();
        storage.create(info, content);
    }

    @Override
    public byte[] retrieve(String key) {
        return storage.readAllBytes(BlobId.of(bucket, key));
    }

    @Override
    public void delete(String key) {
        storage.delete(BlobId.of(bucket, key));
    }

    @Override
    public URI signedUrl(String key, Duration ttl) {
        BlobInfo info = BlobInfo.newBuilder(BlobId.of(bucket, key)).build();
        return URI.create(storage.signUrl(
                info, ttl.toSeconds(), TimeUnit.SECONDS,
                Storage.SignUrlOption.withV4Signature()).toString());
    }
}
