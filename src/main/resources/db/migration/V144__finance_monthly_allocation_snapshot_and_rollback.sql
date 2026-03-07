ALTER TABLE finance_monthly_allocation
  ADD COLUMN elder_snapshot_json TEXT NULL COMMENT '分摊老人快照JSON' AFTER elder_ids,
  ADD COLUMN rollback_by BIGINT NULL COMMENT '回滚操作人' AFTER remark,
  ADD COLUMN rollback_reason VARCHAR(500) NULL COMMENT '回滚原因' AFTER rollback_by,
  ADD COLUMN rollback_time DATETIME NULL COMMENT '回滚时间' AFTER rollback_reason;
