CREATE TABLE assessment_briefs (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    assessment_id UUID        NOT NULL UNIQUE REFERENCES assessments(id) ON DELETE CASCADE,
    learning_goal VARCHAR     NOT NULL,
    topic         VARCHAR     NOT NULL,
    level         VARCHAR     NOT NULL,
    duration      VARCHAR     NOT NULL,
    language      VARCHAR     NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
