package cl.gradeops.ai.api.assessment;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssessmentService {

    /**
     * Returns all assessments for the given teacher.
     *
     * The query is teacher-scoped by design — passing {@code teacherUid} ensures no
     * cross-teacher leakage for the list endpoint.
     *
     * For single-resource methods added in Epic 02+, {@code OwnershipVerifier.verify()}
     * is applied before returning data so that cross-teacher access is denied uniformly.
     *
     * TODO Epic 02: replace with repository query when the assessment table exists.
     */
    public List<AssessmentSummaryDto> listForTeacher(String teacherUid) {
        // TODO Epic 02: replace with repository query when assessment table exists
        return List.of();
    }
}
