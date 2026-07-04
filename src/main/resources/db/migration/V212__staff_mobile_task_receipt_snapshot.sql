-- 员工任务回执补齐任务快照，避免任务完成后上下文丢失

ALTER TABLE operations_staff_task_receipt
  ADD COLUMN task_title VARCHAR(160) DEFAULT NULL COMMENT '任务标题快照' AFTER task_module,
  ADD COLUMN resident VARCHAR(80) DEFAULT NULL COMMENT '服务对象快照' AFTER task_title,
  ADD COLUMN room VARCHAR(80) DEFAULT NULL COMMENT '位置快照' AFTER resident;
