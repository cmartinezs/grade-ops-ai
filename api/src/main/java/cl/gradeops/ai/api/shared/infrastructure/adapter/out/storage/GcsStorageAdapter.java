package cl.gradeops.ai.api.shared.infrastructure.adapter.out.storage;

import com.google.cloud.storage.*;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class GcsStorageAdapter implements StoragePort {

    private final Storage storage;
    private final String bucket;

    public GcsStorageAdapter(Storage storage, String bucket) {
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
