-- 护理部升级：异常事件台账、护理计划审核与版本、整改台账

CREATE TABLE IF NOT EXISTS care_incident (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  org_id BIGINT NOT NULL,
  incident_no VARCHAR(32) NOT NULL COMMENT '事件编号 YC+yyyyMMdd+3位序号',
  elder_id BIGINT NULL,
  elder_name VARCHAR(64) NULL,
  location VARCHAR(128) NULL,
  incident_type VARCHAR(32) NOT NULL COMMENT 'FALL/SKIN/CHOKING/WANDER/DELAY/ENVIRONMENT/OTHER',
  severity VARCHAR(16) NOT NULL COMMENT 'LOW/MEDIUM/HIGH',
  occurred_at DATETIME NOT NULL,
  reporter_id BIGINT NULL,
  reporter_name VARCHAR(64) NULL,
  description TEXT NULL,
  measures TEXT NULL,
  notify_targets VARCHAR(255) NULL,
  owner_id BIGINT NULL,
  owner_name VARCHAR(64) NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'REPORTED' COMMENT 'REPORTED/ACCEPTED/PROCESSING/ESCALATED/PENDING_REVIEW/CLOSED',
  escalate_to VARCHAR(32) NULL COMMENT 'MEDICAL/LOGISTICS/ADMIN_DUTY',
  related_task_id BIGINT NULL,
  reviewer_id BIGINT NULL,
  reviewer_name VARCHAR(64) NULL,
  review_note TEXT NULL,
  closed_at DATETIME NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_care_incident_no (tenant_id, incident_no),
  KEY idx_care_incident_tenant_time (tenant_id, occurred_at),
  KEY idx_care_incident_status (tenant_id, status)
) COMMENT='护理异常事件台账';

CREATE TABLE IF NOT EXISTS care_incident_action (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  incident_id BIGINT NOT NULL,
  action VARCHAR(32) NOT NULL,
  from_status VARCHAR(32) NULL,
  to_status VARCHAR(32) NULL,
  note TEXT NULL,
  operator_id BIGINT NULL,
  operator_name VARCHAR(64) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_care_incident_action_incident (incident_id)
) COMMENT='护理异常事件处置记录（只追加）';

ALTER TABLE service_plan
  ADD COLUMN revision INT NOT NULL DEFAULT 1 COMMENT '计划版本号',
  ADD COLUMN approval_status VARCHAR(16) NOT NULL DEFAULT 'PUBLISHED' COMMENT 'PENDING/PUBLISHED/REJECTED',
  ADD COLUMN approved_by BIGINT NULL,
  ADD COLUMN approved_at DATETIME NULL;

CREATE TABLE IF NOT EXISTS service_plan_revision (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  plan_id BIGINT NOT NULL,
  revision INT NOT NULL,
  snapshot_json TEXT NULL COMMENT '修改前计划快照',
  change_reason VARCHAR(255) NULL,
  changed_by BIGINT NULL,
  changed_by_name VARCHAR(64) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_service_plan_revision_plan (plan_id)
) COMMENT='护理计划历史版本';

CREATE TABLE IF NOT EXISTS nursing_rectification (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  org_id BIGINT NOT NULL,
  title VARCHAR(255) NOT NULL,
  source VARCHAR(64) NULL,
  owner_id BIGINT NULL,
  owner_name VARCHAR(64) NULL,
  due_date DATE NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN/IN_PROGRESS/PENDING_REVIEW/DONE',
  evidence TEXT NULL,
  review_result TEXT NULL,
  reviewer_id BIGINT NULL,
  reviewer_name VARCHAR(64) NULL,
  closed_at DATETIME NULL,
  created_by BIGINT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_nursing_rectification_tenant (tenant_id, status, due_date)
) COMMENT='护理整改台账';
