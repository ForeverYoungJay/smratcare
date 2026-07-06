-- IoT 安全预警闭环：告警联动占位字段（视频/定位）+ 派单升级去向字段，幂等

SET @smart_alert_media_ref_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'smart_alert'
    AND COLUMN_NAME = 'media_ref'
);
SET @ddl = IF(
  @smart_alert_media_ref_exists = 0,
  'ALTER TABLE smart_alert ADD COLUMN media_ref VARCHAR(500) DEFAULT NULL COMMENT ''联动影像引用（厂商适配层预留，如视频片段URL/ID）''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @smart_alert_location_ref_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'smart_alert'
    AND COLUMN_NAME = 'location_ref'
);
SET @ddl = IF(
  @smart_alert_location_ref_exists = 0,
  'ALTER TABLE smart_alert ADD COLUMN location_ref VARCHAR(500) DEFAULT NULL COMMENT ''联动定位引用（厂商适配层预留，如定位轨迹ID/坐标）''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @smart_dispatch_escalated_to_id_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'smart_alert_dispatch'
    AND COLUMN_NAME = 'escalated_to_id'
);
SET @ddl = IF(
  @smart_dispatch_escalated_to_id_exists = 0,
  'ALTER TABLE smart_alert_dispatch ADD COLUMN escalated_to_id BIGINT DEFAULT NULL COMMENT ''升级接收人ID（部长角色）''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @smart_dispatch_escalated_to_name_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'smart_alert_dispatch'
    AND COLUMN_NAME = 'escalated_to_name'
);
SET @ddl = IF(
  @smart_dispatch_escalated_to_name_exists = 0,
  'ALTER TABLE smart_alert_dispatch ADD COLUMN escalated_to_name VARCHAR(60) DEFAULT NULL COMMENT ''升级接收人姓名''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @smart_dispatch_escalated_at_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'smart_alert_dispatch'
    AND COLUMN_NAME = 'escalated_at'
);
SET @ddl = IF(
  @smart_dispatch_escalated_at_exists = 0,
  'ALTER TABLE smart_alert_dispatch ADD COLUMN escalated_at DATETIME DEFAULT NULL COMMENT ''最近升级时间''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
