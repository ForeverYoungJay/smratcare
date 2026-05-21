-- Widen auto_discharge_status to support PENDING_SETTLEMENT without mutating historical migrations.

SET @auto_discharge_status_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder_discharge_apply'
    AND COLUMN_NAME = 'auto_discharge_status'
);

SET @auto_discharge_status_needs_resize = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder_discharge_apply'
    AND COLUMN_NAME = 'auto_discharge_status'
    AND (
      DATA_TYPE <> 'varchar'
      OR CHARACTER_MAXIMUM_LENGTH < 32
      OR COLUMN_COMMENT <> '自动退住结果 SUCCESS/FAILED/PENDING_SETTLEMENT'
    )
);

SET @ddl = IF(
  @auto_discharge_status_exists = 0,
  'ALTER TABLE elder_discharge_apply ADD COLUMN auto_discharge_status VARCHAR(32) DEFAULT NULL COMMENT ''自动退住结果 SUCCESS/FAILED/PENDING_SETTLEMENT'' AFTER linked_discharge_id',
  IF(
    @auto_discharge_status_needs_resize > 0,
    'ALTER TABLE elder_discharge_apply MODIFY COLUMN auto_discharge_status VARCHAR(32) DEFAULT NULL COMMENT ''自动退住结果 SUCCESS/FAILED/PENDING_SETTLEMENT'' AFTER linked_discharge_id',
    'SELECT 1'
  )
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
