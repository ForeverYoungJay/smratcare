-- Marketing report enhancement: contract traceability + report indexes (idempotent)

SET @crm_contract_signed_flag_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_lead'
    AND COLUMN_NAME = 'contract_signed_flag'
);
SET @ddl = IF(
  @crm_contract_signed_flag_exists = 0,
  'ALTER TABLE crm_lead ADD COLUMN contract_signed_flag TINYINT NOT NULL DEFAULT 0 COMMENT ''是否已签约 0否 1是''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_contract_signed_at_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_lead'
    AND COLUMN_NAME = 'contract_signed_at'
);
SET @ddl = IF(
  @crm_contract_signed_at_exists = 0,
  'ALTER TABLE crm_lead ADD COLUMN contract_signed_at DATETIME DEFAULT NULL COMMENT ''签约时间''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_contract_no_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_lead'
    AND COLUMN_NAME = 'contract_no'
);
SET @ddl = IF(
  @crm_contract_no_exists = 0,
  'ALTER TABLE crm_lead ADD COLUMN contract_no VARCHAR(64) DEFAULT NULL COMMENT ''签约合同号''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_crm_create_exists = (
  SELECT COUNT(1)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_lead'
    AND INDEX_NAME = 'idx_crm_lead_tenant_deleted_create'
);
SET @ddl = IF(
  @idx_crm_create_exists = 0,
  'CREATE INDEX idx_crm_lead_tenant_deleted_create ON crm_lead (tenant_id, is_deleted, create_time)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_crm_status_exists = (
  SELECT COUNT(1)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_lead'
    AND INDEX_NAME = 'idx_crm_lead_tenant_deleted_status'
);
SET @ddl = IF(
  @idx_crm_status_exists = 0,
  'CREATE INDEX idx_crm_lead_tenant_deleted_status ON crm_lead (tenant_id, is_deleted, status)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_crm_next_follow_exists = (
  SELECT COUNT(1)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_lead'
    AND INDEX_NAME = 'idx_crm_lead_tenant_deleted_next_follow'
);
SET @ddl = IF(
  @idx_crm_next_follow_exists = 0,
  'CREATE INDEX idx_crm_lead_tenant_deleted_next_follow ON crm_lead (tenant_id, is_deleted, next_follow_date, status)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_crm_source_exists = (
  SELECT COUNT(1)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_lead'
    AND INDEX_NAME = 'idx_crm_lead_tenant_deleted_source'
);
SET @ddl = IF(
  @idx_crm_source_exists = 0,
  'CREATE INDEX idx_crm_lead_tenant_deleted_source ON crm_lead (tenant_id, is_deleted, source)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_crm_creator_exists = (
  SELECT COUNT(1)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_lead'
    AND INDEX_NAME = 'idx_crm_lead_tenant_deleted_creator'
);
SET @ddl = IF(
  @idx_crm_creator_exists = 0,
  'CREATE INDEX idx_crm_lead_tenant_deleted_creator ON crm_lead (tenant_id, is_deleted, created_by)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_admission_date_exists = (
  SELECT COUNT(1)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder_admission'
    AND INDEX_NAME = 'idx_admission_tenant_deleted_date'
);
SET @ddl = IF(
  @idx_admission_date_exists = 0,
  'CREATE INDEX idx_admission_tenant_deleted_date ON elder_admission (tenant_id, is_deleted, admission_date)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
