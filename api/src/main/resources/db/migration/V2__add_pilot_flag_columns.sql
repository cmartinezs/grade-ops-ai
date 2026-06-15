ALTER TABLE teacher
    ADD COLUMN plan_type     VARCHAR(10)              CHECK (plan_type IN ('pilot', 'free', 'paid')),
    ADD COLUMN related_party BOOLEAN                  NOT NULL DEFAULT FALSE,
    ADD COLUMN offer_details TEXT,
    ADD COLUMN evidence_link TEXT,
    ADD COLUMN flag_set_by   VARCHAR(255),
    ADD COLUMN flag_set_at   TIMESTAMP WITH TIME ZONE;
