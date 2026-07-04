ALTER TABLE operations_staff_task_receipt ADD COLUMN IF NOT EXISTS voice_url VARCHAR(512);
ALTER TABLE operations_staff_task_receipt ADD COLUMN IF NOT EXISTS voice_duration_sec INT;
ALTER TABLE incident_report ADD COLUMN IF NOT EXISTS voice_url VARCHAR(512);
ALTER TABLE incident_report ADD COLUMN IF NOT EXISTS voice_duration_sec INT;
