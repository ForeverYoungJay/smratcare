-- 监管上报任务（民政金民工程 / 医保结算等渠道）
CREATE TABLE IF NOT EXISTS gov_report_task (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NULL,
  org_id BIGINT NOT NULL,
  report_type VARCHAR(32) NOT NULL,
  channel VARCHAR(32) NOT NULL,
  period VARCHAR(16) NULL,
  task_status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  trigger_type VARCHAR(16) NOT NULL DEFAULT 'MANUAL',
  record_count INT NOT NULL DEFAULT 0,
  last_error VARCHAR(1000) NULL,
  retry_count INT NOT NULL DEFAULT 0,
  sent_at DATETIME NULL,
  acked_at DATETIME NULL,
  remark VARCHAR(500) NULL,
  created_by BIGINT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_gov_task_org (org_id, channel, report_type, task_status),
  KEY idx_gov_task_period (org_id, period, task_status),
  KEY idx_gov_task_retry (task_status, retry_count)
);
