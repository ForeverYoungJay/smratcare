SET @family_sms_client_ip_exists := (
  SELECT COUNT(1)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'family_sms_code_log'
    AND COLUMN_NAME = 'client_ip'
);
SET @family_sms_client_ip_sql := IF(
  @family_sms_client_ip_exists = 0,
  'ALTER TABLE family_sms_code_log ADD COLUMN client_ip VARCHAR(64) DEFAULT NULL COMMENT ''客户端IP'' AFTER scene',
  'SELECT 1'
);
PREPARE stmt FROM @family_sms_client_ip_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_family_sms_ip_time_exists := (
  SELECT COUNT(1)
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'family_sms_code_log'
    AND INDEX_NAME = 'idx_family_sms_ip_time'
);
SET @idx_family_sms_ip_time_sql := IF(
  @idx_family_sms_ip_time_exists = 0,
  'ALTER TABLE family_sms_code_log ADD INDEX idx_family_sms_ip_time (org_id, client_ip, create_time)',
  'SELECT 1'
);
PREPARE stmt FROM @idx_family_sms_ip_time_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_family_sms_ip_status_exists := (
  SELECT COUNT(1)
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'family_sms_code_log'
    AND INDEX_NAME = 'idx_family_sms_ip_status'
);
SET @idx_family_sms_ip_status_sql := IF(
  @idx_family_sms_ip_status_exists = 0,
  'ALTER TABLE family_sms_code_log ADD INDEX idx_family_sms_ip_status (org_id, client_ip, status, update_time)',
  'SELECT 1'
);
PREPARE stmt FROM @idx_family_sms_ip_status_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
