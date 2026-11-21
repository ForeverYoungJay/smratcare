-- 自动提醒（主库定义见 V279）

CREATE TABLE IF NOT EXISTS finance_reminder (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT DEFAULT NULL,
  org_id BIGINT NOT NULL,
  reminder_type VARCHAR(40) NOT NULL,
  dedupe_key VARCHAR(160) NOT NULL,
  title VARCHAR(200) NOT NULL,
  content VARCHAR(1000) DEFAULT NULL,
  severity VARCHAR(16) NOT NULL DEFAULT 'INFO',
  biz_month VARCHAR(7) DEFAULT NULL,
  elder_id BIGINT DEFAULT NULL,
  room_id BIGINT DEFAULT NULL,
  amount DECIMAL(12,2) DEFAULT NULL,
  action_path VARCHAR(200) DEFAULT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  handled_at DATETIME DEFAULT NULL,
  handled_by BIGINT DEFAULT NULL,
  handle_remark VARCHAR(200) DEFAULT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_finance_reminder_dedupe
  ON finance_reminder (org_id, dedupe_key, is_deleted);
CREATE INDEX IF NOT EXISTS idx_finance_reminder_status
  ON finance_reminder (org_id, status, reminder_type);
