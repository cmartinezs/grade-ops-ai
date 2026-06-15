CREATE TABLE teacher (
    firebase_uid VARCHAR(128)                NOT NULL,
    name         VARCHAR(255)                NOT NULL,
    email        VARCHAR(255)                NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE   NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP WITH TIME ZONE   NOT NULL DEFAULT NOW(),
    CONSTRAINT teacher_pkey PRIMARY KEY (firebase_uid)
);

CREATE UNIQUE INDEX teacher_email_idx ON teacher (email);
