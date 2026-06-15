package cl.gradeops.ai.api.security;

import cl.gradeops.ai.api.common.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OwnershipVerifier {

    private static final Logger log = LoggerFactory.getLogger(OwnershipVerifier.class);

    /**
     * Verifies that {@code authenticatedUid} owns the resource identified by {@code resourceId}.
     *
     * <p>If the UIDs match, the method returns normally. If they differ, a WARN log entry is
     * emitted (Cloud Logging picks it up via the JSON appender) and a
     * {@link ResourceNotFoundException} is thrown — returning HTTP 404, identical to a
     * genuinely non-existent resource, so that cross-teacher access does not reveal whether
     * a resource exists.
     *
     * @param ownerUid          the UID stored on the resource (its actual owner)
     * @param authenticatedUid  the UID of the currently authenticated teacher
     * @param resourceId        the resource identifier used in the log entry and error body
     */
    public void verify(String ownerUid, String authenticatedUid, String resourceId) {
        if (!ownerUid.equals(authenticatedUid)) {
            log.warn("Cross-teacher access denied: authenticatedTeacher={} resource={}",
                    authenticatedUid, resourceId);
            throw new ResourceNotFoundException(resourceId);
        }
    }
}
