ALTER TABLE service_plan
  ADD COLUMN reject_reason VARCHAR(255) NULL COMMENT '最近一次退回原因' AFTER approved_at;
