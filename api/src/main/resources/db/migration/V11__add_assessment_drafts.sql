CREATE TABLE assessment_drafts (
    id                     UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    assessment_id          UUID        NOT NULL REFERENCES assessments(id) ON DELETE CASCADE,
    version_number         INT         NOT NULL,
    previous_version_id    UUID        REFERENCES assessment_drafts(id),
    title                  VARCHAR     NOT NULL,
    context                TEXT        NOT NULL,
    instructions           TEXT        NOT NULL,
    objectives             JSONB       NOT NULL,
    deliverables           JSONB       NOT NULL,
    constraints            JSONB       NOT NULL,
    agent_execution_log_id UUID,
    created_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (assessment_id, version_number)
);

CREATE INDEX idx_assessment_drafts_assessment_id ON assessment_drafts(assessment_id);
