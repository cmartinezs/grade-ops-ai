package cl.gradeops.ai.api.shared.infrastructure.config.security;

import cl.gradeops.ai.api.shared.domain.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OwnershipVerifier {

    private static final Logger log = LoggerFactory.getLogger(OwnershipVerifier.class);

    public void verify(String ownerUid, String authenticatedUid, String resourceId) {
        if (!ownerUid.equals(authenticatedUid)) {
            log.warn("Cross-teacher access denied: authenticatedTeacher={} resource={}",
                    authenticatedUid, resourceId);
            throw new ResourceNotFoundException(resourceId);
        }
    }
}
