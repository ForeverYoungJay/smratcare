ALTER TABLE elder_admission
  ADD COLUMN occupancy_mode VARCHAR(32) NULL COMMENT '入住方式：BED按床位，WHOLE_ROOM整租';

UPDATE elder_admission
SET occupancy_mode = 'BED'
WHERE occupancy_mode IS NULL OR TRIM(occupancy_mode) = '';

