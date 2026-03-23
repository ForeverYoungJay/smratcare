SET @fs_daily_reminder_enabled_exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'fire_safety_record'
    AND COLUMN_NAME = 'daily_reminder_enabled'
);
SET @fs_daily_reminder_time_exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'fire_safety_record'
    AND COLUMN_NAME = 'daily_reminder_time'
);
SET @fs_source_record_id_exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'fire_safety_record'
    AND COLUMN_NAME = 'source_record_id'
);
SET @fs_check_round_exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'fire_safety_record'
    AND COLUMN_NAME = 'check_round'
);

SET @sql = IF(
  @fs_daily_reminder_enabled_exists = 0,
  'ALTER TABLE fire_safety_record ADD COLUMN daily_reminder_enabled TINYINT(1) NOT NULL DEFAULT 0 COMMENT ''是否启用每日提醒检查'' AFTER next_check_date',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
  @fs_daily_reminder_time_exists = 0,
  'ALTER TABLE fire_safety_record ADD COLUMN daily_reminder_time TIME NULL COMMENT ''每日提醒检查时间'' AFTER daily_reminder_enabled',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
  @fs_source_record_id_exists = 0,
  'ALTER TABLE fire_safety_record ADD COLUMN source_record_id BIGINT NULL COMMENT ''来源检查记录ID，用于二次检查链路'' AFTER daily_reminder_time',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
  @fs_check_round_exists = 0,
  'ALTER TABLE fire_safety_record ADD COLUMN check_round INT NOT NULL DEFAULT 1 COMMENT ''检查轮次，首次为1，二次检查递增'' AFTER source_record_id',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
