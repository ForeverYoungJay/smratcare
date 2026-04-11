-- 长者床位关系活跃占用唯一约束，防止同老人/同床位出现多条激活关系

SET @active_elder_lock_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder_bed_relation'
    AND COLUMN_NAME = 'active_elder_lock'
);
SET @ddl = IF(
  @active_elder_lock_exists = 0,
  'ALTER TABLE elder_bed_relation ADD COLUMN active_elder_lock BIGINT GENERATED ALWAYS AS (CASE WHEN active_flag = 1 AND is_deleted = 0 THEN elder_id ELSE NULL END) STORED COMMENT ''活跃老人占床唯一锁'' AFTER is_deleted',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @active_bed_lock_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder_bed_relation'
    AND COLUMN_NAME = 'active_bed_lock'
);
SET @ddl = IF(
  @active_bed_lock_exists = 0,
  'ALTER TABLE elder_bed_relation ADD COLUMN active_bed_lock BIGINT GENERATED ALWAYS AS (CASE WHEN active_flag = 1 AND is_deleted = 0 THEN bed_id ELSE NULL END) STORED COMMENT ''活跃床位占用唯一锁'' AFTER active_elder_lock',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @uk_active_elder_exists = (
  SELECT COUNT(1)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder_bed_relation'
    AND INDEX_NAME = 'uk_elder_bed_rel_active_elder'
);
SET @ddl = IF(
  @uk_active_elder_exists = 0,
  'ALTER TABLE elder_bed_relation ADD UNIQUE KEY uk_elder_bed_rel_active_elder (org_id, active_elder_lock)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @uk_active_bed_exists = (
  SELECT COUNT(1)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder_bed_relation'
    AND INDEX_NAME = 'uk_elder_bed_rel_active_bed'
);
SET @ddl = IF(
  @uk_active_bed_exists = 0,
  'ALTER TABLE elder_bed_relation ADD UNIQUE KEY uk_elder_bed_rel_active_bed (org_id, active_bed_lock)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
