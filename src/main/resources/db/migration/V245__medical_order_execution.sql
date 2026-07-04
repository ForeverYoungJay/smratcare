-- 医嘱执行单（按频次生成计划，执行签核，可关联发药记录）
CREATE TABLE IF NOT EXISTS medical_order_execution (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NULL,
  org_id BIGINT NOT NULL,
  order_id BIGINT NOT NULL,
  elder_id BIGINT NOT NULL,
  plan_time DATETIME NOT NULL,
  exec_time DATETIME NULL,
  executor_id BIGINT NULL,
  executor_name VARCHAR(60) NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  result VARCHAR(500) NULL,
  dispense_record_id BIGINT NULL,
  remark VARCHAR(500) NULL,
  created_by BIGINT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_order_exec_plan (order_id, plan_time, is_deleted),
  KEY idx_order_exec (org_id, order_id, status),
  KEY idx_order_exec_elder (org_id, elder_id, plan_time)
);
