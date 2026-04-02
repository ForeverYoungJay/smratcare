CREATE TABLE IF NOT EXISTS finance_refund_voucher (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  org_id BIGINT NOT NULL,
  settlement_id BIGINT NOT NULL,
  elder_id BIGINT NOT NULL,
  elder_name VARCHAR(64),
  voucher_no VARCHAR(64) NOT NULL,
  amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  status VARCHAR(16) NOT NULL DEFAULT 'PAID',
  pay_method VARCHAR(32),
  executed_by BIGINT,
  executed_by_name VARCHAR(64),
  executed_at DATETIME,
  remark VARCHAR(255),
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_fin_refund_voucher_settlement_del
  ON finance_refund_voucher (org_id, settlement_id, is_deleted);
CREATE UNIQUE INDEX IF NOT EXISTS uk_fin_refund_voucher_no_del
  ON finance_refund_voucher (org_id, voucher_no, is_deleted);

ALTER TABLE bill_monthly ADD COLUMN IF NOT EXISTS elder_contract_id BIGINT;
ALTER TABLE bill_monthly ADD COLUMN IF NOT EXISTS contract_no_snapshot VARCHAR(64);

ALTER TABLE finance_discharge_settlement ADD COLUMN IF NOT EXISTS detail_no VARCHAR(64);
ALTER TABLE finance_discharge_settlement ADD COLUMN IF NOT EXISTS frontdesk_approved TINYINT NOT NULL DEFAULT 0;
ALTER TABLE finance_discharge_settlement ADD COLUMN IF NOT EXISTS frontdesk_signer_name VARCHAR(64);
ALTER TABLE finance_discharge_settlement ADD COLUMN IF NOT EXISTS frontdesk_signed_time DATETIME;
ALTER TABLE finance_discharge_settlement ADD COLUMN IF NOT EXISTS nursing_approved TINYINT NOT NULL DEFAULT 0;
ALTER TABLE finance_discharge_settlement ADD COLUMN IF NOT EXISTS nursing_signer_name VARCHAR(64);
ALTER TABLE finance_discharge_settlement ADD COLUMN IF NOT EXISTS nursing_signed_time DATETIME;
ALTER TABLE finance_discharge_settlement ADD COLUMN IF NOT EXISTS finance_refunded TINYINT NOT NULL DEFAULT 0;
ALTER TABLE finance_discharge_settlement ADD COLUMN IF NOT EXISTS finance_refund_operator_name VARCHAR(64);
ALTER TABLE finance_discharge_settlement ADD COLUMN IF NOT EXISTS finance_refund_time DATETIME;
ALTER TABLE finance_discharge_settlement ADD COLUMN IF NOT EXISTS refund_voucher_id BIGINT;
ALTER TABLE finance_discharge_settlement ADD COLUMN IF NOT EXISTS refund_voucher_no VARCHAR(64);

ALTER TABLE reconciliation_daily ADD COLUMN IF NOT EXISTS total_refund DECIMAL(12,2) NOT NULL DEFAULT 0;
ALTER TABLE reconciliation_daily ADD COLUMN IF NOT EXISTS net_received DECIMAL(12,2) NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS crm_contract (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  org_id BIGINT NOT NULL,
  lead_id BIGINT,
  elder_id BIGINT,
  contract_no VARCHAR(64),
  status VARCHAR(32),
  signed_at DATETIME,
  effective_from DATE,
  effective_to DATE,
  remark VARCHAR(255),
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS crm_lead (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  org_id BIGINT NOT NULL,
  contract_no VARCHAR(64),
  contract_signed_flag TINYINT DEFAULT 0,
  contract_status VARCHAR(64),
  contract_signed_at DATETIME,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);
