package cl.gradeops.ai.api.shared.domain.exception;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends DomainException {

    private final String resourceId;

    public ResourceNotFoundException(String resourceId) {
        super("Resource not found: " + resourceId);
        this.resourceId = resourceId;
    }
}
