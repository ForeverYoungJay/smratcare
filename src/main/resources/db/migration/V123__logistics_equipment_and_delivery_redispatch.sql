SET @add_failure_reason_sql = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.columns
      WHERE table_schema = DATABASE()
        AND table_name = 'dining_delivery_record'
        AND column_name = 'failure_reason'
    ),
    'SELECT 1',
    'ALTER TABLE dining_delivery_record ADD COLUMN failure_reason VARCHAR(255) DEFAULT NULL COMMENT ''未送达原因'' AFTER status'
  )
);
PREPARE stmt FROM @add_failure_reason_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_redispatch_status_sql = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.columns
      WHERE table_schema = DATABASE()
        AND table_name = 'dining_delivery_record'
        AND column_name = 'redispatch_status'
    ),
    'SELECT 1',
    'ALTER TABLE dining_delivery_record ADD COLUMN redispatch_status VARCHAR(16) NOT NULL DEFAULT ''NONE'' COMMENT ''重派状态 NONE/REDISPATCHED'' AFTER failure_reason'
  )
);
PREPARE stmt FROM @add_redispatch_status_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_redispatch_at_sql = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.columns
      WHERE table_schema = DATABASE()
        AND table_name = 'dining_delivery_record'
        AND column_name = 'redispatch_at'
    ),
    'SELECT 1',
    'ALTER TABLE dining_delivery_record ADD COLUMN redispatch_at DATETIME DEFAULT NULL COMMENT ''重派计划时间'' AFTER redispatch_status'
  )
);
PREPARE stmt FROM @add_redispatch_at_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_redispatch_by_name_sql = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.columns
      WHERE table_schema = DATABASE()
        AND table_name = 'dining_delivery_record'
        AND column_name = 'redispatch_by_name'
    ),
    'SELECT 1',
    'ALTER TABLE dining_delivery_record ADD COLUMN redispatch_by_name VARCHAR(64) DEFAULT NULL COMMENT ''重派负责人'' AFTER redispatch_at'
  )
);
PREPARE stmt FROM @add_redispatch_by_name_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_redispatch_remark_sql = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.columns
      WHERE table_schema = DATABASE()
        AND table_name = 'dining_delivery_record'
        AND column_name = 'redispatch_remark'
    ),
    'SELECT 1',
    'ALTER TABLE dining_delivery_record ADD COLUMN redispatch_remark VARCHAR(255) DEFAULT NULL COMMENT ''重派备注'' AFTER redispatch_by_name'
  )
);
PREPARE stmt FROM @add_redispatch_remark_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS logistics_equipment_archive (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  equipment_code VARCHAR(64) NOT NULL COMMENT '设备编码',
  equipment_name VARCHAR(128) NOT NULL COMMENT '设备名称',
  category VARCHAR(64) DEFAULT NULL COMMENT '设备分类',
  brand VARCHAR(64) DEFAULT NULL COMMENT '品牌',
  model_no VARCHAR(128) DEFAULT NULL COMMENT '型号',
  serial_no VARCHAR(128) DEFAULT NULL COMMENT '序列号',
  purchase_date DATE DEFAULT NULL COMMENT '采购日期',
  warranty_until DATE DEFAULT NULL COMMENT '质保到期',
  location VARCHAR(128) DEFAULT NULL COMMENT '所在位置',
  maintainer_name VARCHAR(64) DEFAULT NULL COMMENT '维保负责人',
  last_maintained_at DATETIME DEFAULT NULL COMMENT '最近维保时间',
  next_maintained_at DATETIME DEFAULT NULL COMMENT '下次维保时间',
  status VARCHAR(16) NOT NULL DEFAULT 'ENABLED' COMMENT '状态 ENABLED/DISABLED/MAINTENANCE/SCRAPPED',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_logistics_equipment_org_code (org_id, equipment_code),
  KEY idx_logistics_equipment_org_status (org_id, status),
  KEY idx_logistics_equipment_org_category (org_id, category),
  KEY idx_logistics_equipment_org_next_maintained (org_id, next_maintained_at)
) COMMENT='后勤设备档案';
