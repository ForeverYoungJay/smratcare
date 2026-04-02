CREATE TABLE IF NOT EXISTS finance_refund_voucher (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  settlement_id BIGINT NOT NULL COMMENT '退住结算单ID',
  elder_id BIGINT NOT NULL COMMENT '老人ID',
  elder_name VARCHAR(64) DEFAULT NULL COMMENT '老人姓名',
  voucher_no VARCHAR(64) NOT NULL COMMENT '退款凭证号',
  amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '退款金额',
  status VARCHAR(16) NOT NULL DEFAULT 'PAID' COMMENT 'PENDING/PAID/CANCELLED',
  pay_method VARCHAR(32) DEFAULT NULL COMMENT '退款方式',
  executed_by BIGINT DEFAULT NULL COMMENT '执行人',
  executed_by_name VARCHAR(64) DEFAULT NULL COMMENT '执行人姓名',
  executed_at DATETIME DEFAULT NULL COMMENT '执行时间',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_fin_refund_voucher_settlement_del (org_id, settlement_id, is_deleted),
  UNIQUE KEY uk_fin_refund_voucher_no_del (org_id, voucher_no, is_deleted),
  KEY idx_fin_refund_voucher_org_time (org_id, executed_at),
  KEY idx_fin_refund_voucher_elder (elder_id)
) COMMENT='财务退款凭证';

SET @settlement_refund_voucher_id_exists := (
  SELECT COUNT(1)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'finance_discharge_settlement'
    AND COLUMN_NAME = 'refund_voucher_id'
);
SET @settlement_refund_voucher_id_sql := IF(
  @settlement_refund_voucher_id_exists = 0,
  'ALTER TABLE finance_discharge_settlement ADD COLUMN refund_voucher_id BIGINT DEFAULT NULL COMMENT ''退款凭证ID'' AFTER finance_refund_time',
  'SELECT 1'
);
PREPARE stmt FROM @settlement_refund_voucher_id_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @settlement_refund_voucher_no_exists := (
  SELECT COUNT(1)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'finance_discharge_settlement'
    AND COLUMN_NAME = 'refund_voucher_no'
);
SET @settlement_refund_voucher_no_sql := IF(
  @settlement_refund_voucher_no_exists = 0,
  'ALTER TABLE finance_discharge_settlement ADD COLUMN refund_voucher_no VARCHAR(64) DEFAULT NULL COMMENT ''退款凭证号'' AFTER refund_voucher_id',
  'SELECT 1'
);
PREPARE stmt FROM @settlement_refund_voucher_no_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @reconcile_total_refund_exists := (
  SELECT COUNT(1)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'reconciliation_daily'
    AND COLUMN_NAME = 'total_refund'
);
SET @reconcile_total_refund_sql := IF(
  @reconcile_total_refund_exists = 0,
  'ALTER TABLE reconciliation_daily ADD COLUMN total_refund DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT ''退款总额'' AFTER total_received',
  'SELECT 1'
);
PREPARE stmt FROM @reconcile_total_refund_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @reconcile_net_received_exists := (
  SELECT COUNT(1)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'reconciliation_daily'
    AND COLUMN_NAME = 'net_received'
);
SET @reconcile_net_received_sql := IF(
  @reconcile_net_received_exists = 0,
  'ALTER TABLE reconciliation_daily ADD COLUMN net_received DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT ''净收款'' AFTER total_refund',
  'SELECT 1'
);
PREPARE stmt FROM @reconcile_net_received_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
