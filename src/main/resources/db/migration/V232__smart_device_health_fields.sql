-- IoT 安全预警闭环：设备健康监控字段（电量/信号/固件），幂等

SET @smart_device_battery_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'smart_device'
    AND COLUMN_NAME = 'battery_level'
);
SET @ddl = IF(
  @smart_device_battery_exists = 0,
  'ALTER TABLE smart_device ADD COLUMN battery_level INT DEFAULT NULL COMMENT ''电量百分比 0-100''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @smart_device_signal_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'smart_device'
    AND COLUMN_NAME = 'signal_strength'
);
SET @ddl = IF(
  @smart_device_signal_exists = 0,
  'ALTER TABLE smart_device ADD COLUMN signal_strength INT DEFAULT NULL COMMENT ''信号强度 0-100''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @smart_device_firmware_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'smart_device'
    AND COLUMN_NAME = 'firmware_version'
);
SET @ddl = IF(
  @smart_device_firmware_exists = 0,
  'ALTER TABLE smart_device ADD COLUMN firmware_version VARCHAR(64) DEFAULT NULL COMMENT ''固件版本''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
