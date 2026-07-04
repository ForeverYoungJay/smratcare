-- 上报报文快照与渠道回执
CREATE TABLE IF NOT EXISTS gov_report_snapshot (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NULL,
  org_id BIGINT NOT NULL,
  task_id BIGINT NOT NULL,
  payload_json JSON NULL,
  payload_hash VARCHAR(80) NULL,
  field_mapping_version VARCHAR(64) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_gov_snapshot_task (task_id, is_deleted),
  KEY idx_gov_snapshot_hash (payload_hash)
);

CREATE TABLE IF NOT EXISTS gov_report_receipt (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NULL,
  org_id BIGINT NOT NULL,
  task_id BIGINT NOT NULL,
  receipt_code VARCHAR(80) NULL,
  receipt_status VARCHAR(24) NULL,
  error_detail VARCHAR(1000) NULL,
  received_at DATETIME NULL,
  retry_count INT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_gov_receipt_task (task_id),
  KEY idx_gov_receipt_code (org_id, receipt_code)
);
