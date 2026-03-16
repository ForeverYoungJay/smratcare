SET @elder_discharge_apply_proof_file_url_exists = (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder_discharge_apply'
    AND COLUMN_NAME = 'proof_file_url'
);
SET @elder_discharge_apply_proof_file_url_sql = IF(
  @elder_discharge_apply_proof_file_url_exists = 0,
  'ALTER TABLE elder_discharge_apply ADD COLUMN proof_file_url VARCHAR(500) NULL COMMENT ''退住申请证明附件'' AFTER reason',
  'SELECT 1'
);
PREPARE stmt FROM @elder_discharge_apply_proof_file_url_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
