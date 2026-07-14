CREATE TABLE agent_execution_logs (
    id                      UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    assessment_id           UUID        NOT NULL REFERENCES assessments(id) ON DELETE CASCADE,
    draft_id                UUID        REFERENCES assessment_drafts(id) ON DELETE SET NULL,
    agent_execution_id      UUID,
    agent_name              VARCHAR,
    provider                VARCHAR,
    model                   VARCHAR,
    prompt_version          VARCHAR,
    input_hash              VARCHAR,
    output_hash             VARCHAR,
    estimated_input_tokens  INTEGER,
    estimated_output_tokens INTEGER,
    cost_estimate           DOUBLE PRECISION,
    status                  VARCHAR     NOT NULL,
    error_code              VARCHAR,
    started_at              TIMESTAMPTZ NOT NULL,
    finished_at             TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_agent_execution_logs_assessment_id ON agent_execution_logs(assessment_id);
CREATE INDEX idx_agent_execution_logs_draft_id ON agent_execution_logs(draft_id);

ALTER TABLE assessment_drafts
    ADD CONSTRAINT assessment_drafts_agent_execution_log_id_fkey
    FOREIGN KEY (agent_execution_log_id) REFERENCES agent_execution_logs(id);
