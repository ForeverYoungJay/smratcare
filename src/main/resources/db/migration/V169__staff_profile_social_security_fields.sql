SET @has_social_security_status := (
  SELECT COUNT(1)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'staff_profile'
    AND COLUMN_NAME = 'social_security_status'
);
SET @sql_social_security_status := IF(
  @has_social_security_status = 0,
  'ALTER TABLE staff_profile ADD COLUMN social_security_status VARCHAR(32) DEFAULT NULL COMMENT ''社保状态'' AFTER birthday',
  'SELECT 1'
);
PREPARE stmt FROM @sql_social_security_status;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_social_security_start_date := (
  SELECT COUNT(1)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'staff_profile'
    AND COLUMN_NAME = 'social_security_start_date'
);
SET @sql_social_security_start_date := IF(
  @has_social_security_start_date = 0,
  'ALTER TABLE staff_profile ADD COLUMN social_security_start_date DATE DEFAULT NULL COMMENT ''社保开始缴纳日期'' AFTER social_security_status',
  'SELECT 1'
);
PREPARE stmt FROM @sql_social_security_start_date;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_social_security_reminder_days := (
  SELECT COUNT(1)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'staff_profile'
    AND COLUMN_NAME = 'social_security_reminder_days'
);
SET @sql_social_security_reminder_days := IF(
  @has_social_security_reminder_days = 0,
  'ALTER TABLE staff_profile ADD COLUMN social_security_reminder_days INT DEFAULT NULL COMMENT ''入职后多少天开始提醒缴纳社保'' AFTER social_security_start_date',
  'SELECT 1'
);
PREPARE stmt FROM @sql_social_security_reminder_days;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_social_security_remark := (
  SELECT COUNT(1)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'staff_profile'
    AND COLUMN_NAME = 'social_security_remark'
);
SET @sql_social_security_remark := IF(
  @has_social_security_remark = 0,
  'ALTER TABLE staff_profile ADD COLUMN social_security_remark VARCHAR(500) DEFAULT NULL COMMENT ''社保备注'' AFTER social_security_reminder_days',
  'SELECT 1'
);
PREPARE stmt FROM @sql_social_security_remark;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
