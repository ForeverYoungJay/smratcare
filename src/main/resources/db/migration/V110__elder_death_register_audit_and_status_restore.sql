-- 死亡登记增强：回滚前状态、操作审计字段

SET @has_before_status := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder_death_register'
    AND COLUMN_NAME = 'before_status'
);
SET @sql_before_status := IF(
  @has_before_status = 0,
  'ALTER TABLE elder_death_register ADD COLUMN before_status TINYINT DEFAULT NULL COMMENT ''登记前老人状态'' AFTER reported_time',
  'SELECT 1'
);
PREPARE stmt FROM @sql_before_status;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_updated_by := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder_death_register'
    AND COLUMN_NAME = 'updated_by'
);
SET @sql_updated_by := IF(
  @has_updated_by = 0,
  'ALTER TABLE elder_death_register ADD COLUMN updated_by BIGINT DEFAULT NULL COMMENT ''最后更正人'' AFTER created_by',
  'SELECT 1'
);
PREPARE stmt FROM @sql_updated_by;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_cancelled_by := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder_death_register'
    AND COLUMN_NAME = 'cancelled_by'
);
SET @sql_cancelled_by := IF(
  @has_cancelled_by = 0,
  'ALTER TABLE elder_death_register ADD COLUMN cancelled_by BIGINT DEFAULT NULL COMMENT ''作废操作人'' AFTER updated_by',
  'SELECT 1'
);
PREPARE stmt FROM @sql_cancelled_by;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_cancelled_time := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder_death_register'
    AND COLUMN_NAME = 'cancelled_time'
);
SET @sql_cancelled_time := IF(
  @has_cancelled_time = 0,
  'ALTER TABLE elder_death_register ADD COLUMN cancelled_time DATETIME DEFAULT NULL COMMENT ''作废时间'' AFTER cancelled_by',
  'SELECT 1'
);
PREPARE stmt FROM @sql_cancelled_time;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
