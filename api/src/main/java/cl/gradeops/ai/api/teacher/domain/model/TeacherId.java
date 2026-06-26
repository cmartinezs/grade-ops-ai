package cl.gradeops.ai.api.teacher.domain.model;

import cl.gradeops.ai.api.shared.domain.exception.DomainInvariantViolationException;

public record TeacherId(String value) {
    public TeacherId {
        if (value == null)        throw new DomainInvariantViolationException("firebaseUid must not be null");
        if (value.isBlank())      throw new DomainInvariantViolationException("firebaseUid must not be blank");
    }
}
