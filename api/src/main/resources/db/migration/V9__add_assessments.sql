CREATE TABLE assessments (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    teacher_uid VARCHAR     NOT NULL REFERENCES teacher(firebase_uid) ON DELETE CASCADE,
    status      VARCHAR     NOT NULL DEFAULT 'DRAFT',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_assessments_teacher_uid ON assessments(teacher_uid);
