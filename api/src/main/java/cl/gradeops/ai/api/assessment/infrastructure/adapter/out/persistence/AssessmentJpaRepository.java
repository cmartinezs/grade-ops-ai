package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AssessmentJpaRepository extends JpaRepository<AssessmentJpaEntity, UUID> {
    List<AssessmentJpaEntity> findAllByTeacherUid(String teacherUid);

    /**
     * Single query (no N+1): joins each assessment with its brief (for the {@code topic}
     * fallback) and, via a lateral join, its highest-{@code version_number} draft (for the
     * real title) — {@code COALESCE} prefers the draft's title, falling back to the brief's
     * topic when no draft has been generated yet.
     */
    @Query(value = """
        SELECT a.id AS id, a.status AS status, COALESCE(d.title, b.topic) AS title
        FROM assessments a
        JOIN assessment_briefs b ON b.assessment_id = a.id
        LEFT JOIN LATERAL (
            SELECT ad.title FROM assessment_drafts ad
            WHERE ad.assessment_id = a.id
            ORDER BY ad.version_number DESC
            LIMIT 1
        ) d ON true
        WHERE a.teacher_uid = :teacherUid
        ORDER BY a.created_at DESC
        """, nativeQuery = true)
    List<AssessmentSummaryProjection> findSummariesByTeacherUid(@Param("teacherUid") String teacherUid);
}
