package cl.gradeops.ai.api.auth.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public record RawCode(String value) {

    public RawCode {
        Objects.requireNonNull(value, "rawCode is required");
        if (value.isBlank()) {
            throw new IllegalArgumentException("rawCode must not be blank");
        }
    }

    public static RawCode generate() {
        return new RawCode(UUID.randomUUID().toString());
    }
}
