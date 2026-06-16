package cl.gradeops.ai.api.port;

import java.net.URI;
import java.time.Duration;

public interface StoragePort {
    void store(String key, byte[] content, String contentType);
    byte[] retrieve(String key);
    void delete(String key);
    URI signedUrl(String key, Duration ttl);
}
