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
     * fallback) and, directly via {@code current_revision_id} — the single authoritative pointer
     * (never {@code MAX(version_number)}-style re-derivation, per Idempotency and Concurrency
     * Strategy ADR) — its current {@code AssessmentRevision} (for the real title). {@code
     * COALESCE} prefers the revision's title, falling back to the brief's topic when no revision
     * exists yet (never generated, or a pre-cut legacy assessment).
     */
    @Query(value = """
        SELECT a.id AS id, a.status AS status, COALESCE(r.title, b.topic) AS title
        FROM assessments a
        JOIN assessment_briefs b ON b.assessment_id = a.id
        LEFT JOIN assessment_revisions r ON r.id = a.current_revision_id
        WHERE a.teacher_uid = :teacherUid
        ORDER BY a.created_at DESC
        """, nativeQuery = true)
    List<AssessmentSummaryProjection> findSummariesByTeacherUid(@Param("teacherUid") String teacherUid);
}
