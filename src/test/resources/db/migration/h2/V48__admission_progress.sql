-- 入住办理进度与暂存草稿（主库定义见 V286）

CREATE TABLE IF NOT EXISTS elder_admission_progress (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT DEFAULT NULL,
  org_id BIGINT NOT NULL,
  elder_id BIGINT NOT NULL,
  contract_no VARCHAR(64) DEFAULT NULL,
  current_step INT NOT NULL DEFAULT 1,
  status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
  return_reason VARCHAR(200) DEFAULT NULL,
  draft_json TEXT DEFAULT NULL,
  updated_by BIGINT DEFAULT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_elder_admission_progress
  ON elder_admission_progress (org_id, elder_id, is_deleted);
