-- M2 入住服务增强（增量）：退住申请关联单号/自动退住结果/并发锁

ALTER TABLE elder_discharge_apply
  ADD COLUMN linked_discharge_id BIGINT DEFAULT NULL COMMENT '关联退住登记ID' AFTER status,
  ADD COLUMN auto_discharge_status VARCHAR(16) DEFAULT NULL COMMENT '自动退住结果 SUCCESS/FAILED' AFTER linked_discharge_id,
  ADD COLUMN auto_discharge_message VARCHAR(255) DEFAULT NULL COMMENT '自动退住结果说明' AFTER auto_discharge_status,
  ADD COLUMN reviewed_by_name VARCHAR(64) DEFAULT NULL COMMENT '审核人姓名' AFTER reviewed_by,
  ADD COLUMN pending_lock TINYINT GENERATED ALWAYS AS (
    CASE
      WHEN status = 'PENDING' AND is_deleted = 0 THEN 1
      ELSE NULL
    END
  ) STORED COMMENT '待审核并发锁' AFTER is_deleted;

ALTER TABLE elder_discharge_apply
  ADD KEY idx_discharge_apply_linked_discharge (linked_discharge_id),
  ADD KEY idx_discharge_apply_pending (org_id, elder_id, status, is_deleted),
  ADD UNIQUE KEY uk_discharge_apply_pending (org_id, elder_id, pending_lock);
