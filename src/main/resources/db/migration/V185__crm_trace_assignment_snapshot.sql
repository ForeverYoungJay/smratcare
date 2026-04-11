ALTER TABLE crm_lead
  ADD COLUMN owner_staff_id BIGINT DEFAULT NULL COMMENT '当前负责人ID' AFTER marketer_name,
  ADD COLUMN owner_staff_name VARCHAR(64) DEFAULT NULL COMMENT '当前负责人姓名' AFTER owner_staff_id,
  ADD COLUMN assigned_at DATETIME DEFAULT NULL COMMENT '最近分配时间' AFTER owner_staff_name;

UPDATE crm_lead
SET owner_staff_id = created_by
WHERE owner_staff_id IS NULL
  AND created_by IS NOT NULL;

UPDATE crm_lead
SET owner_staff_name = COALESCE(NULLIF(marketer_name, ''), NULLIF(receptionist_name, ''), NULLIF(name, ''))
WHERE owner_staff_name IS NULL;

UPDATE crm_lead
SET assigned_at = COALESCE(update_time, create_time, NOW())
WHERE assigned_at IS NULL
  AND owner_staff_id IS NOT NULL;

CREATE INDEX idx_crm_lead_owner_staff ON crm_lead(owner_staff_id);

CREATE TABLE IF NOT EXISTS crm_lead_assign_log (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  org_id BIGINT NOT NULL,
  lead_id BIGINT NOT NULL,
  from_owner_staff_id BIGINT DEFAULT NULL,
  from_owner_staff_name VARCHAR(64) DEFAULT NULL,
  to_owner_staff_id BIGINT DEFAULT NULL,
  to_owner_staff_name VARCHAR(64) DEFAULT NULL,
  assigned_by BIGINT DEFAULT NULL,
  assigned_by_name VARCHAR(64) DEFAULT NULL,
  assigned_at DATETIME NOT NULL,
  remark VARCHAR(255) DEFAULT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
) COMMENT='CRM线索分配记录';

CREATE INDEX idx_crm_lead_assign_log_lead ON crm_lead_assign_log(lead_id, assigned_at);

CREATE TABLE IF NOT EXISTS crm_stage_transition_log (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  org_id BIGINT NOT NULL,
  entity_type VARCHAR(32) NOT NULL COMMENT 'LEAD/CONTRACT',
  lead_id BIGINT DEFAULT NULL,
  contract_id BIGINT DEFAULT NULL,
  transition_type VARCHAR(64) DEFAULT NULL COMMENT 'CREATE/UPDATE/HANDOFF/ADMISSION_SYNC等',
  source VARCHAR(64) DEFAULT NULL COMMENT 'SERVICE/UI/SYSTEM',
  from_stage VARCHAR(64) DEFAULT NULL,
  to_stage VARCHAR(64) DEFAULT NULL,
  from_status VARCHAR(64) DEFAULT NULL,
  to_status VARCHAR(64) DEFAULT NULL,
  from_owner_dept VARCHAR(64) DEFAULT NULL,
  to_owner_dept VARCHAR(64) DEFAULT NULL,
  remark VARCHAR(255) DEFAULT NULL,
  operated_by BIGINT DEFAULT NULL,
  operated_by_name VARCHAR(64) DEFAULT NULL,
  operated_at DATETIME NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
) COMMENT='CRM阶段流转日志';

CREATE INDEX idx_crm_stage_transition_log_entity ON crm_stage_transition_log(entity_type, lead_id, contract_id, operated_at);

CREATE TABLE IF NOT EXISTS crm_contract_workflow_log (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  org_id BIGINT NOT NULL,
  contract_id BIGINT NOT NULL,
  lead_id BIGINT DEFAULT NULL,
  action_type VARCHAR(64) NOT NULL,
  before_status VARCHAR(64) DEFAULT NULL,
  after_status VARCHAR(64) DEFAULT NULL,
  before_flow_stage VARCHAR(64) DEFAULT NULL,
  after_flow_stage VARCHAR(64) DEFAULT NULL,
  before_change_workflow VARCHAR(64) DEFAULT NULL,
  after_change_workflow VARCHAR(64) DEFAULT NULL,
  remark VARCHAR(255) DEFAULT NULL,
  snapshot_json LONGTEXT DEFAULT NULL,
  operated_by BIGINT DEFAULT NULL,
  operated_by_name VARCHAR(64) DEFAULT NULL,
  operated_at DATETIME NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
) COMMENT='CRM合同流程日志';

CREATE INDEX idx_crm_contract_workflow_log_contract ON crm_contract_workflow_log(contract_id, operated_at);

CREATE TABLE IF NOT EXISTS crm_sales_report_snapshot (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  org_id BIGINT NOT NULL,
  snapshot_type VARCHAR(64) NOT NULL,
  snapshot_date DATE NOT NULL,
  window_from DATE DEFAULT NULL,
  window_to DATE DEFAULT NULL,
  snapshot_key VARCHAR(128) DEFAULT NULL,
  metrics_json LONGTEXT DEFAULT NULL,
  generated_by BIGINT DEFAULT NULL,
  generated_by_name VARCHAR(64) DEFAULT NULL,
  generated_at DATETIME NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
) COMMENT='CRM销售报表快照';

CREATE INDEX idx_crm_sales_report_snapshot_key ON crm_sales_report_snapshot(snapshot_type, snapshot_date, snapshot_key);
