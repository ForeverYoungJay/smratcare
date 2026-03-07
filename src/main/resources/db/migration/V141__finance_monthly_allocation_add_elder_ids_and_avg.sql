ALTER TABLE finance_monthly_allocation
  ADD COLUMN elder_ids VARCHAR(1024) NULL COMMENT '分摊老人ID列表，逗号分隔' AFTER target_count,
  ADD COLUMN avg_amount DECIMAL(12,2) NULL COMMENT '人均分摊金额' AFTER elder_ids;
