ALTER TABLE finance_discharge_fee_audit
  ADD COLUMN fee_item VARCHAR(64) DEFAULT NULL COMMENT '费用项' AFTER payable_amount,
  ADD COLUMN discharge_fee_config VARCHAR(64) DEFAULT NULL COMMENT '退住费用设置' AFTER fee_item;

ALTER TABLE finance_discharge_settlement
  ADD COLUMN fee_item VARCHAR(64) DEFAULT NULL COMMENT '费用项' AFTER payable_amount,
  ADD COLUMN discharge_fee_config VARCHAR(64) DEFAULT NULL COMMENT '退住费用设置' AFTER fee_item;
