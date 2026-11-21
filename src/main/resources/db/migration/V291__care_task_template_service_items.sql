ALTER TABLE care_task_template
  ADD COLUMN service_items TEXT NULL COMMENT '服务项目清单，每行一项';

-- 执行备注要写下勾选情况、原因和观察，255 不够
ALTER TABLE care_task_execute_log
  MODIFY COLUMN remark VARCHAR(1000) NULL;
