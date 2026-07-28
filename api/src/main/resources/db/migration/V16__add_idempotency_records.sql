CREATE TABLE idempotency_records (
    id                    UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    scope_type            VARCHAR     NOT NULL CHECK (scope_type IN ('TEACHER','ASSESSMENT')),
    teacher_uid           VARCHAR,
    assessment_id         UUID REFERENCES assessments(id),
    operation_type        VARCHAR     NOT NULL,
    idempotency_key       VARCHAR     NOT NULL,
    request_payload_hash  VARCHAR     NOT NULL,
    result_reference       VARCHAR,
    response_status        INT,
    created_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at             TIMESTAMPTZ NOT NULL,
    UNIQUE NULLS NOT DISTINCT (scope_type, teacher_uid, assessment_id, operation_type, idempotency_key)
);
