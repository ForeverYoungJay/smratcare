-- H2 测试库此前没有任何迁移创建 incident_report（主库定义见 V6__m4_m5_oa.sql），
-- 导致下方 ALTER 报 "Table not found"、集成测试上下文无法启动；这里先补建该表。
CREATE TABLE IF NOT EXISTS incident_report (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  org_id BIGINT NOT NULL,
  elder_id BIGINT DEFAULT NULL,
  elder_name VARCHAR(64) DEFAULT NULL,
  reporter_name VARCHAR(64) NOT NULL,
  incident_time TIMESTAMP NOT NULL,
  incident_type VARCHAR(32) NOT NULL,
  level VARCHAR(16) NOT NULL DEFAULT 'NORMAL',
  description VARCHAR(512) NOT NULL,
  action_taken VARCHAR(512) DEFAULT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'OPEN',
  created_by BIGINT DEFAULT NULL,
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_incident_org_time ON incident_report (org_id, incident_time);
CREATE INDEX IF NOT EXISTS idx_incident_elder ON incident_report (elder_id);

ALTER TABLE incident_report ADD COLUMN IF NOT EXISTS emergency_plan CLOB;
ALTER TABLE incident_report ADD COLUMN IF NOT EXISTS onsite_handling CLOB;
ALTER TABLE incident_report ADD COLUMN IF NOT EXISTS family_notification CLOB;
ALTER TABLE incident_report ADD COLUMN IF NOT EXISTS rectification_measure CLOB;
ALTER TABLE incident_report ADD COLUMN IF NOT EXISTS review_conclusion CLOB;
ALTER TABLE incident_report ADD COLUMN IF NOT EXISTS regulatory_report CLOB;
