ALTER TABLE daily_entry
    ADD submitted_at TIMESTAMPTZ;

UPDATE daily_entry
SET submitted_at = entry_date::TIMESTAMP AT TIME ZONE 'UTC';

ALTER TABLE daily_entry
    ALTER COLUMN submitted_at SET NOT NULL;
