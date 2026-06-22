ALTER TABLE password_reset_codes
    ADD CONSTRAINT uq_prc_teacher_uid UNIQUE (teacher_uid);
