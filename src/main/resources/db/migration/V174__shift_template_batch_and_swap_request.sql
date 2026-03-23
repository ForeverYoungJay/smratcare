ALTER TABLE shift_template
  ADD COLUMN rule_sort INT NOT NULL DEFAULT 0 COMMENT '方案内排序' AFTER name;

ALTER TABLE staff_schedule
  ADD COLUMN source_template_id BIGINT NULL COMMENT '来源排班模板ID' AFTER staff_id,
  ADD COLUMN source_template_name VARCHAR(128) NULL COMMENT '来源排班方案名' AFTER source_template_id,
  ADD COLUMN calendar_task_id BIGINT NULL COMMENT '同步日程任务ID' AFTER end_time,
  ADD COLUMN duty_todo_id BIGINT NULL COMMENT '值班提醒待办ID' AFTER calendar_task_id;

CREATE TABLE IF NOT EXISTS shift_swap_request (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NULL,
  org_id BIGINT NULL,
  applicant_staff_id BIGINT NOT NULL,
  applicant_staff_name VARCHAR(64) NULL,
  applicant_schedule_id BIGINT NOT NULL,
  applicant_duty_date DATE NULL,
  applicant_shift_code VARCHAR(64) NULL,
  applicant_start_time DATETIME NULL,
  applicant_end_time DATETIME NULL,
  target_staff_id BIGINT NOT NULL,
  target_staff_name VARCHAR(64) NULL,
  target_schedule_id BIGINT NOT NULL,
  target_duty_date DATE NULL,
  target_shift_code VARCHAR(64) NULL,
  target_start_time DATETIME NULL,
  target_end_time DATETIME NULL,
  status VARCHAR(32) NULL,
  target_confirm_status VARCHAR(32) NULL,
  approval_id BIGINT NULL,
  approval_status VARCHAR(32) NULL,
  applicant_remark VARCHAR(500) NULL,
  target_remark VARCHAR(500) NULL,
  target_confirmed_at DATETIME NULL,
  approval_submitted_at DATETIME NULL,
  completed_at DATETIME NULL,
  created_by BIGINT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT DEFAULT 0,
  KEY idx_shift_swap_org_status (org_id, status, is_deleted),
  KEY idx_shift_swap_target (target_staff_id, target_confirm_status, is_deleted),
  KEY idx_shift_swap_applicant (applicant_staff_id, status, is_deleted),
  KEY idx_shift_swap_approval (approval_id, is_deleted)
) COMMENT='员工换班申请';
