-- 长者床位关系热点查询与并发保护索引

SET @idx_exists = (
  SELECT COUNT(1)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder_bed_relation'
    AND INDEX_NAME = 'idx_elder_bed_rel_org_elder_active_deleted'
);
SET @ddl = IF(
  @idx_exists = 0,
  'CREATE INDEX idx_elder_bed_rel_org_elder_active_deleted ON elder_bed_relation (org_id, elder_id, active_flag, is_deleted)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists = (
  SELECT COUNT(1)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder_bed_relation'
    AND INDEX_NAME = 'idx_elder_bed_rel_org_bed_active_deleted'
);
SET @ddl = IF(
  @idx_exists = 0,
  'CREATE INDEX idx_elder_bed_rel_org_bed_active_deleted ON elder_bed_relation (org_id, bed_id, active_flag, is_deleted)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists = (
  SELECT COUNT(1)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder_bed_relation'
    AND INDEX_NAME = 'idx_elder_bed_rel_tenant_elder_active_deleted'
);
SET @ddl = IF(
  @idx_exists = 0,
  'CREATE INDEX idx_elder_bed_rel_tenant_elder_active_deleted ON elder_bed_relation (tenant_id, elder_id, active_flag, is_deleted)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists = (
  SELECT COUNT(1)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder_bed_relation'
    AND INDEX_NAME = 'idx_elder_bed_rel_tenant_bed_active_deleted'
);
SET @ddl = IF(
  @idx_exists = 0,
  'CREATE INDEX idx_elder_bed_rel_tenant_bed_active_deleted ON elder_bed_relation (tenant_id, bed_id, active_flag, is_deleted)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
