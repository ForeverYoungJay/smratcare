-- M2 入住服务增强（增量）：退住申请关联单号/自动退住结果/并发锁（幂等）

SET @linked_discharge_id_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder_discharge_apply'
    AND COLUMN_NAME = 'linked_discharge_id'
);
SET @ddl = IF(
  @linked_discharge_id_exists = 0,
  'ALTER TABLE elder_discharge_apply ADD COLUMN linked_discharge_id BIGINT DEFAULT NULL COMMENT ''关联退住登记ID'' AFTER status',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @auto_discharge_status_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder_discharge_apply'
    AND COLUMN_NAME = 'auto_discharge_status'
);
SET @ddl = IF(
  @auto_discharge_status_exists = 0,
  'ALTER TABLE elder_discharge_apply ADD COLUMN auto_discharge_status VARCHAR(16) DEFAULT NULL COMMENT ''自动退住结果 SUCCESS/FAILED'' AFTER linked_discharge_id',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @auto_discharge_message_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder_discharge_apply'
    AND COLUMN_NAME = 'auto_discharge_message'
);
SET @ddl = IF(
  @auto_discharge_message_exists = 0,
  'ALTER TABLE elder_discharge_apply ADD COLUMN auto_discharge_message VARCHAR(255) DEFAULT NULL COMMENT ''自动退住结果说明'' AFTER auto_discharge_status',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @reviewed_by_name_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder_discharge_apply'
    AND COLUMN_NAME = 'reviewed_by_name'
);
SET @ddl = IF(
  @reviewed_by_name_exists = 0,
  'ALTER TABLE elder_discharge_apply ADD COLUMN reviewed_by_name VARCHAR(64) DEFAULT NULL COMMENT ''审核人姓名'' AFTER reviewed_by',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @pending_lock_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder_discharge_apply'
    AND COLUMN_NAME = 'pending_lock'
);
SET @ddl = IF(
  @pending_lock_exists = 0,
  'ALTER TABLE elder_discharge_apply ADD COLUMN pending_lock TINYINT GENERATED ALWAYS AS (CASE WHEN status = ''PENDING'' AND is_deleted = 0 THEN 1 ELSE NULL END) STORED COMMENT ''待审核并发锁'' AFTER is_deleted',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_linked_discharge_exists = (
  SELECT COUNT(1)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder_discharge_apply'
    AND INDEX_NAME = 'idx_discharge_apply_linked_discharge'
);
SET @ddl = IF(
  @idx_linked_discharge_exists = 0,
  'CREATE INDEX idx_discharge_apply_linked_discharge ON elder_discharge_apply (linked_discharge_id)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_pending_exists = (
  SELECT COUNT(1)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder_discharge_apply'
    AND INDEX_NAME = 'idx_discharge_apply_pending'
);
SET @ddl = IF(
  @idx_pending_exists = 0,
  'CREATE INDEX idx_discharge_apply_pending ON elder_discharge_apply (org_id, elder_id, status, is_deleted)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @uk_pending_exists = (
  SELECT COUNT(1)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder_discharge_apply'
    AND INDEX_NAME = 'uk_discharge_apply_pending'
);
SET @ddl = IF(
  @uk_pending_exists = 0,
  'ALTER TABLE elder_discharge_apply ADD UNIQUE KEY uk_discharge_apply_pending (org_id, elder_id, pending_lock)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
