package cl.gradeops.ai.api.auth.domain.valueobject;

import cl.gradeops.ai.api.shared.domain.exception.DomainInvariantViolationException;

import java.util.UUID;

public record RawCode(String value) {

    public RawCode {
        if (value == null)   throw new DomainInvariantViolationException("rawCode must not be null");
        if (value.isBlank()) throw new DomainInvariantViolationException("rawCode must not be blank");
    }

    public static RawCode generate() {
        return new RawCode(UUID.randomUUID().toString());
    }
}
