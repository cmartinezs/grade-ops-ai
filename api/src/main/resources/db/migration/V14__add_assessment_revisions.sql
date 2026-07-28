CREATE TABLE assessment_revisions (
    id                       UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    assessment_id            UUID        NOT NULL REFERENCES assessments(id) ON DELETE CASCADE,
    version_number           INT         NOT NULL,
    previous_revision_id     UUID        REFERENCES assessment_revisions(id),
    origin                   VARCHAR     NOT NULL CHECK (origin IN ('AI_GENERATED','HUMAN_EDITED','LEGACY_UNKNOWN')),
    actor_id                 VARCHAR,
    reason                   TEXT,
    source_agent_attempt_id  UUID REFERENCES agent_attempts(id),
    title                    VARCHAR     NOT NULL,
    context                  TEXT        NOT NULL,
    instructions             TEXT        NOT NULL,
    objectives               JSONB       NOT NULL,
    deliverables              JSONB       NOT NULL,
    constraints               JSONB       NOT NULL,
    created_at               TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (assessment_id, version_number)
);

CREATE INDEX idx_assessment_revisions_assessment_id ON assessment_revisions(assessment_id);

ALTER TABLE ai_operations
    ADD CONSTRAINT ai_operations_expected_revision_id_fkey
    FOREIGN KEY (expected_revision_id) REFERENCES assessment_revisions(id),
    ADD CONSTRAINT ai_operations_result_revision_id_fkey
    FOREIGN KEY (result_revision_id) REFERENCES assessment_revisions(id);
