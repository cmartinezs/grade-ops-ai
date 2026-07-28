CREATE TABLE ai_operations (
    id                   UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    assessment_id        UUID        NOT NULL REFERENCES assessments(id) ON DELETE CASCADE,
    operation_type       VARCHAR     NOT NULL CHECK (operation_type IN ('CREATE_INITIAL_REVISION','REGENERATE_REVISION')),
    requested_by         VARCHAR     NOT NULL,
    idempotency_key      VARCHAR     NOT NULL,
    expected_revision_id UUID,
    status               VARCHAR     NOT NULL CHECK (status IN ('PENDING','IN_PROGRESS','SUCCEEDED','FAILED_RETRYABLE','FAILED_TERMINAL')),
    result_revision_id   UUID,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_ai_operations_in_flight
    ON ai_operations(assessment_id, operation_type) WHERE status IN ('PENDING','IN_PROGRESS');

CREATE TABLE agent_attempts (
    id                      UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    ai_operation_id         UUID        NOT NULL REFERENCES ai_operations(id) ON DELETE CASCADE,
    attempt_number          INT         NOT NULL,
    agent_name              VARCHAR     NOT NULL,
    resolved_provider       VARCHAR,
    resolved_model          VARCHAR,
    prompt_version          VARCHAR,
    correlation_id          VARCHAR,
    dispatched_at           TIMESTAMPTZ NOT NULL,
    completed_at            TIMESTAMPTZ,
    status                  VARCHAR     NOT NULL CHECK (status IN ('DISPATCHED','COMPLETED','FAILED')),
    provider_request_id     VARCHAR,
    failure_code            VARCHAR,
    estimated_input_tokens  INTEGER,
    estimated_output_tokens INTEGER,
    cost_estimate           NUMERIC(12,6),
    structured_result        JSONB,
    UNIQUE (ai_operation_id, attempt_number)
);

CREATE INDEX idx_agent_attempts_ai_operation_id ON agent_attempts(ai_operation_id);
