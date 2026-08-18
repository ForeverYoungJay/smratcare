-- 押金管理（主库定义见 V278）

CREATE TABLE IF NOT EXISTS finance_deposit_standard (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT DEFAULT NULL,
  org_id BIGINT NOT NULL,
  standard_name VARCHAR(100) NOT NULL,
  care_level VARCHAR(32) DEFAULT NULL,
  amount DECIMAL(12,2) NOT NULL,
  effective_from DATE NOT NULL,
  remark VARCHAR(200) DEFAULT NULL,
  created_by BIGINT DEFAULT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS finance_deposit_account (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT DEFAULT NULL,
  org_id BIGINT NOT NULL,
  elder_id BIGINT NOT NULL,
  standard_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  paid_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  deducted_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  refunded_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  balance_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  status VARCHAR(16) NOT NULL DEFAULT 'UNPAID',
  last_op_at DATETIME DEFAULT NULL,
  remark VARCHAR(200) DEFAULT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_deposit_account_elder
  ON finance_deposit_account (org_id, elder_id, is_deleted);

CREATE TABLE IF NOT EXISTS finance_deposit_transaction (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT DEFAULT NULL,
  org_id BIGINT NOT NULL,
  deposit_account_id BIGINT NOT NULL,
  elder_id BIGINT NOT NULL,
  txn_type VARCHAR(16) NOT NULL,
  amount DECIMAL(12,2) NOT NULL,
  balance_after DECIMAL(12,2) NOT NULL DEFAULT 0,
  pay_method VARCHAR(32) DEFAULT NULL,
  occurred_at DATETIME NOT NULL,
  reason VARCHAR(200) DEFAULT NULL,
  remark VARCHAR(200) DEFAULT NULL,
  operator_staff_id BIGINT DEFAULT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_deposit_txn_account
  ON finance_deposit_transaction (org_id, deposit_account_id, is_deleted);
