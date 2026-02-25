CREATE TABLE IF NOT EXISTS assessment_record (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  org_id BIGINT NOT NULL,
  elder_id BIGINT,
  elder_name VARCHAR(64) NOT NULL,
  assessment_type VARCHAR(32) NOT NULL,
  template_id BIGINT,
  level_code VARCHAR(32),
  score DECIMAL(8,2),
  assessment_date DATE NOT NULL,
  next_assessment_date DATE,
  assessor_id BIGINT,
  assessor_name VARCHAR(64),
  status VARCHAR(16) NOT NULL DEFAULT 'COMPLETED',
  result_summary VARCHAR(500),
  suggestion VARCHAR(500),
  detail_json CLOB,
  score_auto TINYINT NOT NULL DEFAULT 1,
  archive_no VARCHAR(64),
  source VARCHAR(32),
  created_by BIGINT,
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS assessment_scale_template (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  org_id BIGINT NOT NULL,
  template_code VARCHAR(64) NOT NULL,
  template_name VARCHAR(128) NOT NULL,
  assessment_type VARCHAR(32) NOT NULL,
  description VARCHAR(255),
  score_rules_json CLOB,
  level_rules_json CLOB,
  status TINYINT NOT NULL DEFAULT 1,
  created_by BIGINT,
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS assessment_reminder_log (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  org_id BIGINT NOT NULL,
  record_id BIGINT NOT NULL,
  remind_date DATE NOT NULL,
  todo_id BIGINT,
  status VARCHAR(16) NOT NULL DEFAULT 'CREATED',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS oa_todo (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  org_id BIGINT NOT NULL,
  title VARCHAR(255) NOT NULL,
  content VARCHAR(1000),
  due_time TIMESTAMP,
  status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
  assignee_id BIGINT,
  assignee_name VARCHAR(64),
  created_by BIGINT,
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

INSERT INTO assessment_scale_template (
  id, tenant_id, org_id, template_code, template_name, assessment_type, description,
  score_rules_json, level_rules_json, status, created_by, is_deleted
)
SELECT
  91001, 1, 1, 'TEST_SELF_CARE', '测试自理量表', 'SELF_CARE', '测试模板',
  '[{"key":"q1","weight":1},{"key":"q2","weight":0.5}]',
  '[{"min":0,"max":4,"level":"LOW"},{"min":4.01,"max":20,"level":"HIGH"}]',
  1, 500, 0
WHERE NOT EXISTS (
  SELECT 1 FROM assessment_scale_template WHERE id = 91001
);
