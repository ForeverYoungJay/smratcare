-- H2 版本：与 MySQL V295 一致（去掉注释与普通索引）
-- 人力资源：工资批次与工资单（核算、审核、财务提交银行、付款回执分离）、考勤月度锁定与调整单、绩效/积分申诉

CREATE TABLE IF NOT EXISTS hr_payroll_batch (
  id BIGINT NOT NULL PRIMARY KEY,
  org_id BIGINT NOT NULL,
  period VARCHAR(7) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
  headcount INT NOT NULL DEFAULT 0,
  total_gross DECIMAL(14,2) NOT NULL DEFAULT 0,
  total_net DECIMAL(14,2) NOT NULL DEFAULT 0,
  prepared_by BIGINT NULL,
  prepared_by_name VARCHAR(64) NULL,
  approved_by BIGINT NULL,
  approved_by_name VARCHAR(64) NULL,
  approved_at DATETIME NULL,
  reject_reason VARCHAR(255) NULL,
  bank_submitted_by_name VARCHAR(64) NULL,
  bank_submitted_at DATETIME NULL,
  payment_ref VARCHAR(128) NULL,
  paid_by_name VARCHAR(64) NULL,
  paid_at DATETIME NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS hr_payroll_line (
  id BIGINT NOT NULL PRIMARY KEY,
  org_id BIGINT NOT NULL,
  batch_id BIGINT NOT NULL,
  staff_id BIGINT NOT NULL,
  staff_name VARCHAR(64) NULL,
  base_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  performance_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  allowance_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  overtime_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  deduction_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  personal_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  gross_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  net_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  remark VARCHAR(255) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_hr_payroll_line UNIQUE (batch_id, staff_id)
);

CREATE TABLE IF NOT EXISTS hr_attendance_period (
  id BIGINT NOT NULL PRIMARY KEY,
  org_id BIGINT NOT NULL,
  period VARCHAR(7) NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'LOCKED',
  locked_by_name VARCHAR(64) NULL,
  locked_at DATETIME NULL,
  CONSTRAINT uk_hr_attendance_period UNIQUE (org_id, period)
);

CREATE TABLE IF NOT EXISTS hr_attendance_adjustment (
  id BIGINT NOT NULL PRIMARY KEY,
  org_id BIGINT NOT NULL,
  record_id BIGINT NOT NULL,
  staff_id BIGINT NULL,
  work_date DATE NULL,
  new_check_in DATETIME NULL,
  new_check_out DATETIME NULL,
  new_status VARCHAR(32) NULL,
  reason VARCHAR(255) NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  requested_by BIGINT NULL,
  requested_by_name VARCHAR(64) NULL,
  decided_by_name VARCHAR(64) NULL,
  decided_at DATETIME NULL,
  decision_note VARCHAR(255) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS hr_performance_appeal (
  id BIGINT NOT NULL PRIMARY KEY,
  org_id BIGINT NOT NULL,
  staff_id BIGINT NOT NULL,
  staff_name VARCHAR(64) NULL,
  target_type VARCHAR(32) NOT NULL,
  target_id BIGINT NULL,
  reason VARCHAR(500) NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  reviewer_name VARCHAR(64) NULL,
  reviewed_at DATETIME NULL,
  decision_note VARCHAR(255) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 测试库缺少员工服务方案表（离职清单查宿舍登记用），按 MySQL V172 补建
CREATE TABLE IF NOT EXISTS hr_staff_service_plan (
  id BIGINT NOT NULL,
  org_id BIGINT NOT NULL,
  staff_id BIGINT NOT NULL,
  staff_no VARCHAR(64) DEFAULT NULL,
  staff_name VARCHAR(64) DEFAULT NULL,
  department_id BIGINT DEFAULT NULL,
  department_name VARCHAR(64) DEFAULT NULL,
  breakfast_enabled TINYINT NOT NULL DEFAULT 0,
  breakfast_unit_price DECIMAL(10,2) DEFAULT NULL,
  breakfast_days_per_month INT DEFAULT NULL,
  lunch_enabled TINYINT NOT NULL DEFAULT 0,
  lunch_unit_price DECIMAL(10,2) DEFAULT NULL,
  lunch_days_per_month INT DEFAULT NULL,
  dinner_enabled TINYINT NOT NULL DEFAULT 0,
  dinner_unit_price DECIMAL(10,2) DEFAULT NULL,
  dinner_days_per_month INT DEFAULT NULL,
  live_in_dormitory TINYINT NOT NULL DEFAULT 0,
  dormitory_building VARCHAR(64) DEFAULT NULL,
  dormitory_room_no VARCHAR(64) DEFAULT NULL,
  dormitory_bed_no VARCHAR(64) DEFAULT NULL,
  meter_no VARCHAR(64) DEFAULT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'ENABLED',
  remark VARCHAR(255) DEFAULT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  CONSTRAINT uk_hr_staff_service_plan_org_staff UNIQUE (org_id, staff_id)
);
