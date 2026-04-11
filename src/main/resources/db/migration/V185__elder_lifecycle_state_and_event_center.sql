-- 长者生命周期主状态字段与事件中心

SET @elder_lifecycle_status_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder'
    AND COLUMN_NAME = 'lifecycle_status'
);
SET @ddl = IF(
  @elder_lifecycle_status_exists = 0,
  'ALTER TABLE elder ADD COLUMN lifecycle_status VARCHAR(32) DEFAULT NULL COMMENT ''统一生命周期主状态'' AFTER status',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @elder_departure_type_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder'
    AND COLUMN_NAME = 'departure_type'
);
SET @ddl = IF(
  @elder_departure_type_exists = 0,
  'ALTER TABLE elder ADD COLUMN departure_type VARCHAR(16) DEFAULT NULL COMMENT ''离场类型 NORMAL/DEATH'' AFTER lifecycle_status',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @elder_lifecycle_updated_at_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder'
    AND COLUMN_NAME = 'lifecycle_updated_at'
);
SET @ddl = IF(
  @elder_lifecycle_updated_at_exists = 0,
  'ALTER TABLE elder ADD COLUMN lifecycle_updated_at DATETIME DEFAULT NULL COMMENT ''生命周期状态更新时间'' AFTER departure_type',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @elder_last_lifecycle_event_id_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder'
    AND COLUMN_NAME = 'last_lifecycle_event_id'
);
SET @ddl = IF(
  @elder_last_lifecycle_event_id_exists = 0,
  'ALTER TABLE elder ADD COLUMN last_lifecycle_event_id BIGINT DEFAULT NULL COMMENT ''最新生命周期事件ID'' AFTER lifecycle_updated_at',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE elder
SET lifecycle_status = CASE
    WHEN status = 1 THEN 'IN_HOSPITAL'
    WHEN status = 2 THEN 'OUTING'
    WHEN status = 3 THEN 'DISCHARGED'
    ELSE 'INTENT'
  END
WHERE lifecycle_status IS NULL;

UPDATE elder e
JOIN elder_death_register d
  ON d.elder_id = e.id
 AND d.is_deleted = 0
 AND d.status = 'REGISTERED'
SET e.lifecycle_status = 'DECEASED',
    e.departure_type = 'DEATH',
    e.lifecycle_updated_at = COALESCE(e.lifecycle_updated_at, NOW())
WHERE e.is_deleted = 0;

UPDATE elder e
JOIN elder_discharge_apply a
  ON a.elder_id = e.id
 AND a.is_deleted = 0
 AND a.status = 'APPROVED'
SET e.lifecycle_status = CASE
      WHEN e.lifecycle_status IN ('DISCHARGED', 'DECEASED') THEN e.lifecycle_status
      ELSE 'DISCHARGE_PENDING'
    END,
    e.departure_type = CASE
      WHEN e.lifecycle_status = 'DECEASED' THEN 'DEATH'
      ELSE e.departure_type
    END,
    e.lifecycle_updated_at = COALESCE(e.lifecycle_updated_at, NOW())
WHERE e.is_deleted = 0;

CREATE TABLE IF NOT EXISTS elder_lifecycle_event (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  elder_id BIGINT NOT NULL COMMENT '老人ID',
  event_type VARCHAR(32) NOT NULL COMMENT '事件类型',
  from_lifecycle_status VARCHAR(32) DEFAULT NULL COMMENT '迁移前主状态',
  to_lifecycle_status VARCHAR(32) DEFAULT NULL COMMENT '迁移后主状态',
  from_departure_type VARCHAR(16) DEFAULT NULL COMMENT '迁移前离场类型',
  to_departure_type VARCHAR(16) DEFAULT NULL COMMENT '迁移后离场类型',
  biz_ref_type VARCHAR(32) DEFAULT NULL COMMENT '业务引用类型',
  biz_ref_id BIGINT DEFAULT NULL COMMENT '业务引用ID',
  reason VARCHAR(255) DEFAULT NULL COMMENT '原因',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  KEY idx_elder_lifecycle_event_tenant (tenant_id),
  KEY idx_elder_lifecycle_event_org (org_id),
  KEY idx_elder_lifecycle_event_elder (elder_id),
  KEY idx_elder_lifecycle_event_type (event_type),
  KEY idx_elder_lifecycle_event_ref (biz_ref_type, biz_ref_id)
) COMMENT='长者生命周期事件';
