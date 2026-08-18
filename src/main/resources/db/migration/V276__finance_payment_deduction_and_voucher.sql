-- 收款抵扣计算器：应收 − 长护险抵扣 − 折扣减免 − 消费券抵扣 = 实收现金
-- 1) payment_record 记录本次登记的应收快照、三项抵扣与抵账合计（amount 仍只表示实收现金）
-- 2) 新增消费券主表与核销流水表

-- ---------- payment_record 抵扣字段（幂等） ----------
SET @pr_payable_exists = (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'payment_record' AND COLUMN_NAME = 'payable_amount'
);
SET @ddl = IF(@pr_payable_exists = 0,
  'ALTER TABLE payment_record ADD COLUMN payable_amount DECIMAL(12,2) NULL COMMENT ''登记时应收余额快照''',
  'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @pr_ltci_exists = (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'payment_record' AND COLUMN_NAME = 'ltci_deduct_amount'
);
SET @ddl = IF(@pr_ltci_exists = 0,
  'ALTER TABLE payment_record ADD COLUMN ltci_deduct_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT ''长护险抵扣''',
  'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @pr_discount_exists = (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'payment_record' AND COLUMN_NAME = 'discount_amount'
);
SET @ddl = IF(@pr_discount_exists = 0,
  'ALTER TABLE payment_record ADD COLUMN discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT ''折扣减免''',
  'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @pr_discount_reason_exists = (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'payment_record' AND COLUMN_NAME = 'discount_reason'
);
SET @ddl = IF(@pr_discount_reason_exists = 0,
  'ALTER TABLE payment_record ADD COLUMN discount_reason VARCHAR(200) NULL COMMENT ''折扣减免原因''',
  'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @pr_voucher_exists = (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'payment_record' AND COLUMN_NAME = 'voucher_amount'
);
SET @ddl = IF(@pr_voucher_exists = 0,
  'ALTER TABLE payment_record ADD COLUMN voucher_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT ''消费券抵扣''',
  'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @pr_settled_exists = (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'payment_record' AND COLUMN_NAME = 'settled_amount'
);
SET @ddl = IF(@pr_settled_exists = 0,
  'ALTER TABLE payment_record ADD COLUMN settled_amount DECIMAL(12,2) NULL COMMENT ''本次抵账合计=实收+三项抵扣''',
  'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 历史数据：抵账合计等于当时的实收金额
UPDATE payment_record SET settled_amount = amount WHERE settled_amount IS NULL;

-- ---------- 消费券主表 ----------
CREATE TABLE IF NOT EXISTS finance_consumer_voucher (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NULL,
  org_id BIGINT NOT NULL,
  elder_id BIGINT NULL COMMENT '绑定长者，NULL 表示机构通用券',
  voucher_no VARCHAR(64) NOT NULL COMMENT '券号',
  voucher_name VARCHAR(100) NOT NULL COMMENT '券名称',
  face_amount DECIMAL(12,2) NOT NULL COMMENT '面额',
  balance_amount DECIMAL(12,2) NOT NULL COMMENT '剩余可用额度',
  min_bill_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '使用门槛：账单应收下限',
  allow_split TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许拆分多次使用',
  valid_from DATE NULL,
  valid_to DATE NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/USED/EXPIRED/REVOKED',
  source VARCHAR(32) NULL COMMENT '发放来源',
  issued_by BIGINT NULL,
  issued_at DATETIME NULL,
  revoked_by BIGINT NULL,
  revoked_at DATETIME NULL,
  revoke_reason VARCHAR(200) NULL,
  remark VARCHAR(500) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_consumer_voucher_no (org_id, voucher_no, is_deleted),
  KEY idx_consumer_voucher_elder (org_id, elder_id, status, is_deleted),
  KEY idx_consumer_voucher_valid (org_id, status, valid_to)
) COMMENT '消费券';

-- ---------- 消费券核销流水 ----------
CREATE TABLE IF NOT EXISTS finance_consumer_voucher_usage (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NULL,
  org_id BIGINT NOT NULL,
  voucher_id BIGINT NOT NULL,
  elder_id BIGINT NULL,
  bill_monthly_id BIGINT NULL,
  payment_record_id BIGINT NULL,
  amount DECIMAL(12,2) NOT NULL COMMENT '核销金额，RELEASE 方向为退回金额',
  direction VARCHAR(16) NOT NULL DEFAULT 'USE' COMMENT 'USE 核销 / RELEASE 退回',
  operator_staff_id BIGINT NULL,
  remark VARCHAR(200) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_voucher_usage_voucher (org_id, voucher_id, is_deleted),
  KEY idx_voucher_usage_payment (payment_record_id),
  KEY idx_voucher_usage_bill (org_id, bill_monthly_id)
) COMMENT '消费券核销流水';
