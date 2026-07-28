ALTER TABLE assessments
    ADD COLUMN current_revision_id UUID REFERENCES assessment_revisions(id),
    ADD COLUMN lock_version INT NOT NULL DEFAULT 0;
