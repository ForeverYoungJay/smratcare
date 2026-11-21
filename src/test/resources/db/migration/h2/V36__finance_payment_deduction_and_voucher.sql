-- 收款抵扣计算器（主库定义见 V276）：payment_record 抵扣字段 + 消费券主表与核销流水

ALTER TABLE payment_record ADD COLUMN IF NOT EXISTS payable_amount DECIMAL(12,2);
ALTER TABLE payment_record ADD COLUMN IF NOT EXISTS ltci_deduct_amount DECIMAL(12,2) NOT NULL DEFAULT 0;
ALTER TABLE payment_record ADD COLUMN IF NOT EXISTS discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0;
ALTER TABLE payment_record ADD COLUMN IF NOT EXISTS discount_reason VARCHAR(200);
ALTER TABLE payment_record ADD COLUMN IF NOT EXISTS voucher_amount DECIMAL(12,2) NOT NULL DEFAULT 0;
ALTER TABLE payment_record ADD COLUMN IF NOT EXISTS settled_amount DECIMAL(12,2);

UPDATE payment_record SET settled_amount = amount WHERE settled_amount IS NULL;

CREATE TABLE IF NOT EXISTS finance_consumer_voucher (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT DEFAULT NULL,
  org_id BIGINT NOT NULL,
  elder_id BIGINT DEFAULT NULL,
  voucher_no VARCHAR(64) NOT NULL,
  voucher_name VARCHAR(100) NOT NULL,
  face_amount DECIMAL(12,2) NOT NULL,
  balance_amount DECIMAL(12,2) NOT NULL,
  min_bill_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  allow_split TINYINT NOT NULL DEFAULT 1,
  valid_from DATE DEFAULT NULL,
  valid_to DATE DEFAULT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  source VARCHAR(32) DEFAULT NULL,
  issued_by BIGINT DEFAULT NULL,
  issued_at DATETIME DEFAULT NULL,
  revoked_by BIGINT DEFAULT NULL,
  revoked_at DATETIME DEFAULT NULL,
  revoke_reason VARCHAR(200) DEFAULT NULL,
  remark VARCHAR(500) DEFAULT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_consumer_voucher_no
  ON finance_consumer_voucher (org_id, voucher_no, is_deleted);
CREATE INDEX IF NOT EXISTS idx_consumer_voucher_elder
  ON finance_consumer_voucher (org_id, elder_id, status, is_deleted);

CREATE TABLE IF NOT EXISTS finance_consumer_voucher_usage (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT DEFAULT NULL,
  org_id BIGINT NOT NULL,
  voucher_id BIGINT NOT NULL,
  elder_id BIGINT DEFAULT NULL,
  bill_monthly_id BIGINT DEFAULT NULL,
  payment_record_id BIGINT DEFAULT NULL,
  amount DECIMAL(12,2) NOT NULL,
  direction VARCHAR(16) NOT NULL DEFAULT 'USE',
  operator_staff_id BIGINT DEFAULT NULL,
  remark VARCHAR(200) DEFAULT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_voucher_usage_voucher
  ON finance_consumer_voucher_usage (org_id, voucher_id, is_deleted);
CREATE INDEX IF NOT EXISTS idx_voucher_usage_payment
  ON finance_consumer_voucher_usage (payment_record_id);

-- 长护险月度结算单（主库定义见 V220，金额单位：分）：
-- 收款抵扣校验会读取本表的统筹支付额，H2 测试库需要补建
CREATE TABLE IF NOT EXISTS ltci_settlement (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT DEFAULT NULL,
  org_id BIGINT NOT NULL,
  elder_id BIGINT NOT NULL,
  benefit_id BIGINT DEFAULT NULL,
  settle_month VARCHAR(6) NOT NULL,
  service_days INT NOT NULL DEFAULT 0,
  total_fee BIGINT NOT NULL DEFAULT 0,
  fund_pay BIGINT NOT NULL DEFAULT 0,
  self_pay BIGINT NOT NULL DEFAULT 0,
  over_quota BIGINT NOT NULL DEFAULT 0,
  daily_quota BIGINT DEFAULT NULL,
  pay_ratio DECIMAL(5,4) DEFAULT NULL,
  settle_status VARCHAR(24) NOT NULL DEFAULT 'DRAFT',
  settle_no VARCHAR(64) DEFAULT NULL,
  detail_json CLOB DEFAULT NULL,
  submitted_by BIGINT DEFAULT NULL,
  submitted_at DATETIME DEFAULT NULL,
  remark VARCHAR(500) DEFAULT NULL,
  created_by BIGINT DEFAULT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_ltci_settle_elder_month
  ON ltci_settlement (org_id, elder_id, settle_month, is_deleted);
