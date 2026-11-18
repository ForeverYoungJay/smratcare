-- 人力资源：工资批次与工资单（核算、审核、财务提交银行、付款回执分离）、考勤月度锁定与调整单、绩效/积分申诉

CREATE TABLE IF NOT EXISTS hr_payroll_batch (
  id BIGINT NOT NULL PRIMARY KEY,
  org_id BIGINT NOT NULL,
  period VARCHAR(7) NOT NULL COMMENT '工资期间 YYYY-MM',
  status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/SUBMITTED/APPROVED/BANK_SUBMITTED/PAID',
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
  payment_ref VARCHAR(128) NULL COMMENT '银行付款回执/流水号',
  paid_by_name VARCHAR(64) NULL,
  paid_at DATETIME NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_hr_payroll_batch_org (org_id, period)
) COMMENT='工资批次';

CREATE TABLE IF NOT EXISTS hr_payroll_line (
  id BIGINT NOT NULL PRIMARY KEY,
  org_id BIGINT NOT NULL,
  batch_id BIGINT NOT NULL,
  staff_id BIGINT NOT NULL,
  staff_name VARCHAR(64) NULL,
  base_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '基本工资',
  performance_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '绩效',
  allowance_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '津补贴',
  overtime_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '加班（须已审批）',
  deduction_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '依法依规扣款',
  personal_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '个人负担社保公积金个税',
  gross_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  net_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  remark VARCHAR(255) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_hr_payroll_line (batch_id, staff_id)
) COMMENT='工资单明细';

CREATE TABLE IF NOT EXISTS hr_attendance_period (
  id BIGINT NOT NULL PRIMARY KEY,
  org_id BIGINT NOT NULL,
  period VARCHAR(7) NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'LOCKED',
  locked_by_name VARCHAR(64) NULL,
  locked_at DATETIME NULL,
  UNIQUE KEY uk_hr_attendance_period (org_id, period)
) COMMENT='考勤月度核定锁定';

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
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
  requested_by BIGINT NULL,
  requested_by_name VARCHAR(64) NULL,
  decided_by_name VARCHAR(64) NULL,
  decided_at DATETIME NULL,
  decision_note VARCHAR(255) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_hr_attendance_adjustment (org_id, status)
) COMMENT='考勤调整单（已锁定期间唯一修改途径）';

CREATE TABLE IF NOT EXISTS hr_performance_appeal (
  id BIGINT NOT NULL PRIMARY KEY,
  org_id BIGINT NOT NULL,
  staff_id BIGINT NOT NULL,
  staff_name VARCHAR(64) NULL,
  target_type VARCHAR(32) NOT NULL COMMENT 'POINTS_LOG/REWARD_PUNISHMENT/PERFORMANCE',
  target_id BIGINT NULL,
  reason VARCHAR(500) NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/ACCEPTED/REJECTED',
  reviewer_name VARCHAR(64) NULL,
  reviewed_at DATETIME NULL,
  decision_note VARCHAR(255) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_hr_performance_appeal (org_id, status)
) COMMENT='绩效与积分申诉';
