-- AI 智能排班：机构级排班约束配置（每机构一行，org_id 唯一）
CREATE TABLE IF NOT EXISTS ai_schedule_constraint (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NULL,
  org_id BIGINT NULL,
  max_weekly_hours DECIMAL(6,2) NOT NULL DEFAULT 40,
  max_consecutive_days INT NOT NULL DEFAULT 5,
  night_rest_enabled TINYINT NOT NULL DEFAULT 1,
  respect_leave TINYINT NOT NULL DEFAULT 1,
  qualification_json VARCHAR(2000) NULL,
  workload_balance_weight DECIMAL(6,2) NOT NULL DEFAULT 1,
  night_fairness_weight DECIMAL(6,2) NOT NULL DEFAULT 1,
  enabled TINYINT NOT NULL DEFAULT 1,
  remark VARCHAR(500) NULL,
  created_by BIGINT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_ai_sched_constraint_org (org_id, is_deleted)
);
