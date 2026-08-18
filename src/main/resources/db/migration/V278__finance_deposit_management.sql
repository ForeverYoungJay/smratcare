-- 押金管理：押金标准 + 每位长者的押金账户 + 缴纳/扣款/退还流水
-- 在押余额 = 已缴 − 已扣 − 已退；应缴差额 = 标准金额 − 已缴

CREATE TABLE IF NOT EXISTS finance_deposit_standard (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NULL,
  org_id BIGINT NOT NULL,
  standard_name VARCHAR(100) NOT NULL COMMENT '标准名称',
  care_level VARCHAR(32) NULL COMMENT '适用护理等级，NULL 表示通用默认标准',
  amount DECIMAL(12,2) NOT NULL COMMENT '押金标准金额',
  effective_from DATE NOT NULL COMMENT '生效日期',
  remark VARCHAR(200) NULL,
  created_by BIGINT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_deposit_standard_org (org_id, care_level, effective_from)
) COMMENT='押金标准';

CREATE TABLE IF NOT EXISTS finance_deposit_account (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NULL,
  org_id BIGINT NOT NULL,
  elder_id BIGINT NOT NULL,
  standard_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '应缴押金标准快照',
  paid_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '累计已缴',
  deducted_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '累计已扣',
  refunded_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '累计已退',
  balance_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '在押余额',
  status VARCHAR(16) NOT NULL DEFAULT 'UNPAID' COMMENT 'UNPAID 未缴 / PARTIAL 部分缴 / PAID 已缴清 / CLOSED 已结清退出',
  last_op_at DATETIME NULL,
  remark VARCHAR(200) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_deposit_account_elder (org_id, elder_id, is_deleted),
  KEY idx_deposit_account_status (org_id, status)
) COMMENT='长者押金账户';

CREATE TABLE IF NOT EXISTS finance_deposit_transaction (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NULL,
  org_id BIGINT NOT NULL,
  deposit_account_id BIGINT NOT NULL,
  elder_id BIGINT NOT NULL,
  txn_type VARCHAR(16) NOT NULL COMMENT 'PAY 缴纳 / DEDUCT 扣款 / REFUND 退还',
  amount DECIMAL(12,2) NOT NULL,
  balance_after DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '本次操作后的在押余额',
  pay_method VARCHAR(32) NULL,
  occurred_at DATETIME NOT NULL,
  reason VARCHAR(200) NULL COMMENT '扣款/退还原因',
  remark VARCHAR(200) NULL,
  operator_staff_id BIGINT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_deposit_txn_account (org_id, deposit_account_id, is_deleted),
  KEY idx_deposit_txn_elder (org_id, elder_id, occurred_at)
) COMMENT='押金流水';
