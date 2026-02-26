ALTER TABLE finance_discharge_settlement
  ADD COLUMN detail_no VARCHAR(64) DEFAULT NULL COMMENT '退住明细单号' AFTER id,
  ADD COLUMN frontdesk_approved TINYINT NOT NULL DEFAULT 0 COMMENT '前台审核通过 0否1是' AFTER status,
  ADD COLUMN frontdesk_signer_name VARCHAR(64) DEFAULT NULL COMMENT '前台签字人' AFTER frontdesk_approved,
  ADD COLUMN frontdesk_signed_time DATETIME DEFAULT NULL COMMENT '前台签字时间' AFTER frontdesk_signer_name,
  ADD COLUMN nursing_approved TINYINT NOT NULL DEFAULT 0 COMMENT '护理部审核通过 0否1是' AFTER frontdesk_signed_time,
  ADD COLUMN nursing_signer_name VARCHAR(64) DEFAULT NULL COMMENT '护理部签字人' AFTER nursing_approved,
  ADD COLUMN nursing_signed_time DATETIME DEFAULT NULL COMMENT '护理部签字时间' AFTER nursing_signer_name,
  ADD COLUMN finance_refunded TINYINT NOT NULL DEFAULT 0 COMMENT '财务已退款 0否1是' AFTER nursing_signed_time,
  ADD COLUMN finance_refund_operator_name VARCHAR(64) DEFAULT NULL COMMENT '财务退款人' AFTER finance_refunded,
  ADD COLUMN finance_refund_time DATETIME DEFAULT NULL COMMENT '财务退款时间' AFTER finance_refund_operator_name;

ALTER TABLE finance_discharge_settlement
  ADD UNIQUE KEY uk_fin_settlement_detail_no (org_id, detail_no);
