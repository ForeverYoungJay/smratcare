-- 合同域与账单绑定增强：营销签署、长者只读、账单引用签署合同

CREATE TABLE IF NOT EXISTS crm_contract (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  lead_id BIGINT DEFAULT NULL COMMENT '营销线索ID',
  elder_id BIGINT DEFAULT NULL COMMENT '长者ID',
  contract_no VARCHAR(64) NOT NULL COMMENT '合同编号',
  status VARCHAR(32) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/PENDING_APPROVAL/APPROVED/SIGNED/EFFECTIVE/VOID',
  signed_at DATETIME DEFAULT NULL COMMENT '签署时间',
  effective_from DATE DEFAULT NULL COMMENT '生效开始',
  effective_to DATE DEFAULT NULL COMMENT '生效结束',
  marketer_name VARCHAR(64) DEFAULT NULL COMMENT '营销人员',
  snapshot_json LONGTEXT DEFAULT NULL COMMENT '签署快照',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  UNIQUE KEY uk_crm_contract_tenant_no (tenant_id, contract_no, is_deleted),
  KEY idx_crm_contract_tenant_status (tenant_id, is_deleted, status),
  KEY idx_crm_contract_elder (tenant_id, elder_id, is_deleted),
  KEY idx_crm_contract_lead (tenant_id, lead_id, is_deleted)
) COMMENT='CRM合同主表';

SET @crm_contract_attachment_contract_id_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_contract_attachment'
    AND COLUMN_NAME = 'contract_id'
);
SET @crm_contract_attachment_contract_id_sql = IF(
  @crm_contract_attachment_contract_id_exists = 0,
  'ALTER TABLE crm_contract_attachment ADD COLUMN contract_id BIGINT DEFAULT NULL COMMENT ''合同ID'' AFTER lead_id',
  'SELECT 1'
);
PREPARE stmt FROM @crm_contract_attachment_contract_id_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_contract_attachment_type_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_contract_attachment'
    AND COLUMN_NAME = 'attachment_type'
);
SET @crm_contract_attachment_type_sql = IF(
  @crm_contract_attachment_type_exists = 0,
  'ALTER TABLE crm_contract_attachment ADD COLUMN attachment_type VARCHAR(32) DEFAULT ''CONTRACT'' COMMENT ''附件类型 CONTRACT/INVOICE/OTHER'' AFTER contract_id',
  'SELECT 1'
);
PREPARE stmt FROM @crm_contract_attachment_type_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @bill_monthly_elder_contract_id_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'bill_monthly'
    AND COLUMN_NAME = 'elder_contract_id'
);
SET @bill_monthly_elder_contract_id_sql = IF(
  @bill_monthly_elder_contract_id_exists = 0,
  'ALTER TABLE bill_monthly ADD COLUMN elder_contract_id BIGINT DEFAULT NULL COMMENT ''履约合同ID'' AFTER elder_id',
  'SELECT 1'
);
PREPARE stmt FROM @bill_monthly_elder_contract_id_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @bill_monthly_contract_no_snapshot_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'bill_monthly'
    AND COLUMN_NAME = 'contract_no_snapshot'
);
SET @bill_monthly_contract_no_snapshot_sql = IF(
  @bill_monthly_contract_no_snapshot_exists = 0,
  'ALTER TABLE bill_monthly ADD COLUMN contract_no_snapshot VARCHAR(64) DEFAULT NULL COMMENT ''合同号快照'' AFTER elder_contract_id',
  'SELECT 1'
);
PREPARE stmt FROM @bill_monthly_contract_no_snapshot_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
