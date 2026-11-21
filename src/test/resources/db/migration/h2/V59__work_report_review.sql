-- H2 版本：与 MySQL V296 一致
ALTER TABLE oa_work_report ADD COLUMN IF NOT EXISTS reviewer_id BIGINT;
ALTER TABLE oa_work_report ADD COLUMN IF NOT EXISTS reviewer_name VARCHAR(64);
ALTER TABLE oa_work_report ADD COLUMN IF NOT EXISTS review_comment VARCHAR(1000);
ALTER TABLE oa_work_report ADD COLUMN IF NOT EXISTS review_score INT;
ALTER TABLE oa_work_report ADD COLUMN IF NOT EXISTS reviewed_at DATETIME;

-- 测试库缺少考勤季节规则表（考勤概览依赖），按 MySQL V138/V145 补建
CREATE TABLE IF NOT EXISTS attendance_season_rule (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  org_id BIGINT NOT NULL,
  winter_work_start VARCHAR(8) NOT NULL DEFAULT '13:30',
  winter_work_end VARCHAR(8) NOT NULL DEFAULT '17:00',
  summer_work_start VARCHAR(8) NOT NULL DEFAULT '14:30',
  summer_work_end VARCHAR(8) NOT NULL DEFAULT '18:00',
  late_grace_minutes INT NOT NULL DEFAULT 10,
  early_leave_grace_minutes INT NOT NULL DEFAULT 10,
  outing_max_minutes INT NOT NULL DEFAULT 180,
  late_enabled TINYINT NOT NULL DEFAULT 1,
  early_leave_enabled TINYINT NOT NULL DEFAULT 1,
  outing_overtime_enabled TINYINT NOT NULL DEFAULT 1,
  missing_checkout_enabled TINYINT NOT NULL DEFAULT 1,
  enabled_status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  created_by BIGINT DEFAULT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

-- 测试库员工档案、培训/证书表缺少的列（与实体一致），供「我的信息」读取本人档案
ALTER TABLE staff_profile ADD COLUMN IF NOT EXISTS contract_no VARCHAR(255);
ALTER TABLE staff_profile ADD COLUMN IF NOT EXISTS contract_type VARCHAR(255);
ALTER TABLE staff_profile ADD COLUMN IF NOT EXISTS contract_status VARCHAR(255);
ALTER TABLE staff_profile ADD COLUMN IF NOT EXISTS contract_start_date DATE;
ALTER TABLE staff_profile ADD COLUMN IF NOT EXISTS contract_end_date DATE;
ALTER TABLE staff_profile ADD COLUMN IF NOT EXISTS birthday DATE;
ALTER TABLE staff_profile ADD COLUMN IF NOT EXISTS social_security_status VARCHAR(255);
ALTER TABLE staff_profile ADD COLUMN IF NOT EXISTS social_security_start_date DATE;
ALTER TABLE staff_profile ADD COLUMN IF NOT EXISTS social_security_reminder_days INT;
ALTER TABLE staff_profile ADD COLUMN IF NOT EXISTS social_security_company_apply INT;
ALTER TABLE staff_profile ADD COLUMN IF NOT EXISTS social_security_need_director_approval INT;
ALTER TABLE staff_profile ADD COLUMN IF NOT EXISTS social_security_workflow_status VARCHAR(255);
ALTER TABLE staff_profile ADD COLUMN IF NOT EXISTS social_security_monthly_amount DECIMAL(12,2);
ALTER TABLE staff_profile ADD COLUMN IF NOT EXISTS social_security_apply_submitted_at DATETIME;
ALTER TABLE staff_profile ADD COLUMN IF NOT EXISTS social_security_apply_submitted_by BIGINT;
ALTER TABLE staff_profile ADD COLUMN IF NOT EXISTS social_security_director_decision_at DATETIME;
ALTER TABLE staff_profile ADD COLUMN IF NOT EXISTS social_security_director_decision_by BIGINT;
ALTER TABLE staff_profile ADD COLUMN IF NOT EXISTS social_security_finance_todo_id BIGINT;
ALTER TABLE staff_profile ADD COLUMN IF NOT EXISTS social_security_completed_at DATETIME;
ALTER TABLE staff_profile ADD COLUMN IF NOT EXISTS social_security_last_billed_month VARCHAR(255);
ALTER TABLE staff_profile ADD COLUMN IF NOT EXISTS social_security_remark VARCHAR(255);
ALTER TABLE staff_profile ADD COLUMN IF NOT EXISTS leave_date DATE;
ALTER TABLE staff_profile ADD COLUMN IF NOT EXISTS leave_reason VARCHAR(255);
ALTER TABLE staff_training_record ADD COLUMN IF NOT EXISTS source_training_id BIGINT;
ALTER TABLE staff_training_record ADD COLUMN IF NOT EXISTS training_scene VARCHAR(255);
ALTER TABLE staff_training_record ADD COLUMN IF NOT EXISTS training_year INT;
ALTER TABLE staff_training_record ADD COLUMN IF NOT EXISTS department_id BIGINT;
ALTER TABLE staff_training_record ADD COLUMN IF NOT EXISTS department_name VARCHAR(255);
ALTER TABLE staff_training_record ADD COLUMN IF NOT EXISTS staff_no VARCHAR(255);
ALTER TABLE staff_training_record ADD COLUMN IF NOT EXISTS instructor VARCHAR(255);
ALTER TABLE staff_training_record ADD COLUMN IF NOT EXISTS attendance_status VARCHAR(255);
ALTER TABLE staff_training_record ADD COLUMN IF NOT EXISTS test_result VARCHAR(255);
ALTER TABLE staff_training_record ADD COLUMN IF NOT EXISTS certificate_required INT;
ALTER TABLE staff_training_record ADD COLUMN IF NOT EXISTS certificate_status VARCHAR(255);
ALTER TABLE staff_training_record ADD COLUMN IF NOT EXISTS attachments_json VARCHAR(255);
