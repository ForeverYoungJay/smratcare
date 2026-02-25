-- Marketing report enhancement: contract traceability + report indexes

ALTER TABLE crm_lead
  ADD COLUMN IF NOT EXISTS contract_signed_flag TINYINT NOT NULL DEFAULT 0 COMMENT '是否已签约 0否 1是',
  ADD COLUMN IF NOT EXISTS contract_signed_at DATETIME DEFAULT NULL COMMENT '签约时间',
  ADD COLUMN IF NOT EXISTS contract_no VARCHAR(64) DEFAULT NULL COMMENT '签约合同号';

CREATE INDEX IF NOT EXISTS idx_crm_lead_tenant_deleted_create
  ON crm_lead (tenant_id, is_deleted, create_time);
CREATE INDEX IF NOT EXISTS idx_crm_lead_tenant_deleted_status
  ON crm_lead (tenant_id, is_deleted, status);
CREATE INDEX IF NOT EXISTS idx_crm_lead_tenant_deleted_next_follow
  ON crm_lead (tenant_id, is_deleted, next_follow_date, status);
CREATE INDEX IF NOT EXISTS idx_crm_lead_tenant_deleted_source
  ON crm_lead (tenant_id, is_deleted, source);
CREATE INDEX IF NOT EXISTS idx_crm_lead_tenant_deleted_creator
  ON crm_lead (tenant_id, is_deleted, created_by);

CREATE INDEX IF NOT EXISTS idx_admission_tenant_deleted_date
  ON elder_admission (tenant_id, is_deleted, admission_date);
