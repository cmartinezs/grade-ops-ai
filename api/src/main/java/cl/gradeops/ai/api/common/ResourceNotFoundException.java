package cl.gradeops.ai.api.common;

public class ResourceNotFoundException extends RuntimeException {

    private final String resourceId;

    public ResourceNotFoundException(String resourceId) {
        super("Resource not found: " + resourceId);
        this.resourceId = resourceId;
    }

    public String getResourceId() { return resourceId; }
}
