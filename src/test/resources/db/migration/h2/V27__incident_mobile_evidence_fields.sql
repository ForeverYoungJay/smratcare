ALTER TABLE incident_report ADD COLUMN IF NOT EXISTS location VARCHAR(128);
ALTER TABLE incident_report ADD COLUMN IF NOT EXISTS scan_text VARCHAR(255);
ALTER TABLE incident_report ADD COLUMN IF NOT EXISTS attachment_urls CLOB;
