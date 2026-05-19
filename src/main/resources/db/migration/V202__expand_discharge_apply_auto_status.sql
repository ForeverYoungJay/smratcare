SET @auto_discharge_status_type = (
  SELECT COALESCE(DATA_TYPE, '')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder_discharge_apply'
    AND COLUMN_NAME = 'auto_discharge_status'
  LIMIT 1
);

SET @auto_discharge_status_length = (
  SELECT COALESCE(CHARACTER_MAXIMUM_LENGTH, 0)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder_discharge_apply'
    AND COLUMN_NAME = 'auto_discharge_status'
  LIMIT 1
);

SET @ddl = IF(
  @auto_discharge_status_type = 'varchar' AND @auto_discharge_status_length < 32,
  'ALTER TABLE elder_discharge_apply MODIFY COLUMN auto_discharge_status VARCHAR(32) DEFAULT NULL COMMENT ''自动退住结果 SUCCESS/FAILED/PENDING_SETTLEMENT''',
  'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
