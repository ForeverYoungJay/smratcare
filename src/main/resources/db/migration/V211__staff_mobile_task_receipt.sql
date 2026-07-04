-- 员工移动端任务执行回执留痕

CREATE TABLE IF NOT EXISTS operations_staff_task_receipt (
  id BIGINT PRIMARY KEY COMMENT '主键',
  tenant_id BIGINT DEFAULT NULL COMMENT '租户ID',
  org_id BIGINT DEFAULT NULL COMMENT '机构ID',
  staff_id BIGINT DEFAULT NULL COMMENT '员工ID',
  task_id VARCHAR(64) NOT NULL COMMENT '移动端任务ID',
  task_module VARCHAR(32) DEFAULT NULL COMMENT '任务模块',
  remark TEXT COMMENT '回执备注',
  scan_text VARCHAR(255) DEFAULT NULL COMMENT '扫码结果',
  checked_items TEXT COMMENT '检查项结果',
  photo_urls TEXT COMMENT '照片URL，逗号分隔',
  receipt_time DATETIME NOT NULL COMMENT '回执时间',
  status VARCHAR(32) DEFAULT 'DONE' COMMENT '回执状态',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT DEFAULT 0 COMMENT '是否删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工移动端任务执行回执';

CREATE INDEX idx_staff_task_receipt_org_time ON operations_staff_task_receipt (org_id, receipt_time);
CREATE INDEX idx_staff_task_receipt_task_id ON operations_staff_task_receipt (task_id);
CREATE INDEX idx_staff_task_receipt_staff_time ON operations_staff_task_receipt (staff_id, receipt_time);
