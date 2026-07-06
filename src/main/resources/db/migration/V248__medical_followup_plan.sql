-- 慢病随访计划（病种、频次、目标指标、下次随访日期）
CREATE TABLE IF NOT EXISTS medical_followup_plan (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NULL,
  org_id BIGINT NOT NULL,
  elder_id BIGINT NOT NULL,
  elder_name VARCHAR(64) NULL,
  disease_type VARCHAR(32) NOT NULL DEFAULT 'HYPERTENSION',
  plan_name VARCHAR(120) NULL,
  frequency_days INT NOT NULL DEFAULT 30,
  target_indicators VARCHAR(1000) NULL,
  next_followup_date DATE NULL,
  last_followup_date DATE NULL,
  doctor_id BIGINT NULL,
  doctor_name VARCHAR(60) NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  remark VARCHAR(500) NULL,
  created_by BIGINT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_followup_plan_elder (org_id, elder_id, status),
  KEY idx_followup_plan_due (org_id, status, next_followup_date)
);
