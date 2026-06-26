package cl.gradeops.ai.api.teacher.domain.model;

import java.util.Objects;

public record TeacherId(String value) {
    public TeacherId {
        Objects.requireNonNull(value, "firebaseUid must not be null");
        if (value.isBlank()) throw new IllegalArgumentException("firebaseUid must not be blank");
    }
}
