ALTER TABLE elder_discharge_apply ADD COLUMN IF NOT EXISTS care_handover_time TIMESTAMP DEFAULT NULL;
ALTER TABLE elder_discharge_apply ADD COLUMN IF NOT EXISTS care_handover_by_name VARCHAR(64) DEFAULT NULL;
ALTER TABLE elder_discharge_apply ADD COLUMN IF NOT EXISTS item_handover_time TIMESTAMP DEFAULT NULL;
ALTER TABLE elder_discharge_apply ADD COLUMN IF NOT EXISTS item_handover_by_name VARCHAR(64) DEFAULT NULL;
ALTER TABLE elder_discharge_apply ADD COLUMN IF NOT EXISTS archive_time TIMESTAMP DEFAULT NULL;
ALTER TABLE elder_discharge_apply ADD COLUMN IF NOT EXISTS archive_by_name VARCHAR(64) DEFAULT NULL;
