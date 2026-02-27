SET @schema_name = DATABASE();

SET @ddl = IF(
  EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = @schema_name
      AND table_name = 'crm_lead'
      AND column_name = 'reservation_bed_id'
  ),
  'SELECT 1',
  'ALTER TABLE crm_lead ADD COLUMN reservation_bed_id BIGINT DEFAULT NULL COMMENT ''预定床位ID'' AFTER reservation_room_no'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
  EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = @schema_name
      AND table_name = 'crm_lead'
      AND column_name = 'flow_stage'
  ),
  'SELECT 1',
  'ALTER TABLE crm_lead ADD COLUMN flow_stage VARCHAR(32) DEFAULT NULL COMMENT ''流程阶段 PENDING_ASSESSMENT/PENDING_BED_SELECT/PENDING_SIGN/SIGNED'' AFTER contract_status'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
  EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = @schema_name
      AND table_name = 'crm_lead'
      AND column_name = 'current_owner_dept'
  ),
  'SELECT 1',
  'ALTER TABLE crm_lead ADD COLUMN current_owner_dept VARCHAR(32) DEFAULT NULL COMMENT ''当前责任部门 MARKETING/ASSESSMENT'' AFTER flow_stage'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE crm_lead
SET flow_stage = CASE
  WHEN contract_signed_flag = 1 THEN 'SIGNED'
  WHEN flow_stage IS NOT NULL AND flow_stage <> '' THEN flow_stage
  ELSE 'PENDING_ASSESSMENT'
END
WHERE flow_stage IS NULL OR flow_stage = '';

UPDATE crm_lead
SET current_owner_dept = CASE
  WHEN flow_stage = 'PENDING_ASSESSMENT' THEN 'ASSESSMENT'
  ELSE 'MARKETING'
END
WHERE current_owner_dept IS NULL OR current_owner_dept = '';

SET @ddl = IF(
  EXISTS (
    SELECT 1
    FROM information_schema.statistics
    WHERE table_schema = @schema_name
      AND table_name = 'crm_lead'
      AND index_name = 'idx_crm_lead_flow_stage'
  ),
  'SELECT 1',
  'CREATE INDEX idx_crm_lead_flow_stage ON crm_lead (tenant_id, is_deleted, flow_stage)'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
  EXISTS (
    SELECT 1
    FROM information_schema.statistics
    WHERE table_schema = @schema_name
      AND table_name = 'crm_lead'
      AND index_name = 'idx_crm_lead_owner_dept'
  ),
  'SELECT 1',
  'CREATE INDEX idx_crm_lead_owner_dept ON crm_lead (tenant_id, is_deleted, current_owner_dept)'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
