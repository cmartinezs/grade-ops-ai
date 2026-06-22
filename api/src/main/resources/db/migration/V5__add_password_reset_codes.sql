CREATE TABLE password_reset_codes (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    teacher_uid VARCHAR     NOT NULL REFERENCES teacher(firebase_uid) ON DELETE CASCADE,
    code        VARCHAR(36) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ NOT NULL,
    used_at     TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_prc_teacher_uid ON password_reset_codes(teacher_uid);
