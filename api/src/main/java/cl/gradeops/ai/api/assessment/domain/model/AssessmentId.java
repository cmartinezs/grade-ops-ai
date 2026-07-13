package cl.gradeops.ai.api.assessment.domain.model;

import cl.gradeops.ai.api.shared.domain.exception.DomainInvariantViolationException;

import java.util.UUID;

public record AssessmentId(UUID value) {
    public AssessmentId {
        if (value == null) throw new DomainInvariantViolationException("assessment id must not be null");
    }
}
