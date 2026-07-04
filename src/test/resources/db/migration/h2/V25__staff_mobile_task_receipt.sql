CREATE TABLE IF NOT EXISTS operations_staff_task_receipt (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT,
  org_id BIGINT,
  staff_id BIGINT,
  task_id VARCHAR(64) NOT NULL,
  task_module VARCHAR(32),
  remark CLOB,
  scan_text VARCHAR(255),
  checked_items CLOB,
  photo_urls CLOB,
  receipt_time TIMESTAMP NOT NULL,
  status VARCHAR(32) DEFAULT 'DONE',
  created_by BIGINT,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_staff_task_receipt_org_time ON operations_staff_task_receipt (org_id, receipt_time);
CREATE INDEX IF NOT EXISTS idx_staff_task_receipt_task_id ON operations_staff_task_receipt (task_id);
CREATE INDEX IF NOT EXISTS idx_staff_task_receipt_staff_time ON operations_staff_task_receipt (staff_id, receipt_time);
