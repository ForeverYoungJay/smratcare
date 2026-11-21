-- H2 版本：与 MySQL V292 结构一致（去掉注释与索引），另补测试用 fire_safety_record 表
-- 消防安全：楼栋点位与检查模板（管理员配置）、控制室交接双方确认、维保报告签署、
-- 录像调阅申请与审计、消防培训演练台账

CREATE TABLE IF NOT EXISTS fire_location (
  id BIGINT NOT NULL PRIMARY KEY,
  org_id BIGINT NOT NULL,
  building VARCHAR(64) NOT NULL,
  floor_name VARCHAR(32) NULL,
  area VARCHAR(64) NULL,
  point_code VARCHAR(64) NULL,
  risk_level VARCHAR(16) NOT NULL DEFAULT 'MEDIUM',
  key_part TINYINT NOT NULL DEFAULT 0,
  enabled TINYINT NOT NULL DEFAULT 1,
  remark VARCHAR(255) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);--'消防楼栋与巡查点位';

CREATE TABLE IF NOT EXISTS fire_check_template (
  id BIGINT NOT NULL PRIMARY KEY,
  org_id BIGINT NOT NULL,
  record_type VARCHAR(32) NOT NULL,
  template_name VARCHAR(64) NOT NULL,
  items TEXT NOT NULL,
  version INT NOT NULL DEFAULT 1,
  enabled TINYINT NOT NULL DEFAULT 1,
  updated_by_name VARCHAR(64) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);--'消防检查模板';

CREATE TABLE IF NOT EXISTS fire_duty_handover (
  id BIGINT NOT NULL PRIMARY KEY,
  org_id BIGINT NOT NULL,
  shift_date DATE NOT NULL,
  shift_name VARCHAR(32) NOT NULL,
  handover_staff_id BIGINT NOT NULL,
  handover_name VARCHAR(64) NULL,
  takeover_staff_id BIGINT NOT NULL,
  takeover_name VARCHAR(64) NULL,
  checklist TEXT NULL,
  open_issues VARCHAR(1024) NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  handover_at DATETIME NOT NULL,
  takeover_at DATETIME NULL,
  takeover_note VARCHAR(512) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);--'消防控制室交接班（交班、接班双方确认）';

CREATE TABLE IF NOT EXISTS fire_report_signature (
  id BIGINT NOT NULL PRIMARY KEY,
  org_id BIGINT NOT NULL,
  record_id BIGINT NOT NULL,
  sign_type VARCHAR(16) NOT NULL,
  signer_id BIGINT NOT NULL,
  signer_name VARCHAR(64) NULL,
  file_url VARCHAR(512) NULL,
  file_sha256 VARCHAR(64) NULL,
  comment VARCHAR(512) NULL,
  signed_at DATETIME NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);--'维保报告签署记录（只增不改）';

CREATE TABLE IF NOT EXISTS fire_video_access_request (
  id BIGINT NOT NULL PRIMARY KEY,
  org_id BIGINT NOT NULL,
  camera_id BIGINT NOT NULL,
  camera_name VARCHAR(128) NULL,
  range_start DATETIME NOT NULL,
  range_end DATETIME NOT NULL,
  reason VARCHAR(512) NOT NULL,
  applicant_id BIGINT NOT NULL,
  applicant_name VARCHAR(64) NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  approver_id BIGINT NULL,
  approver_name VARCHAR(64) NULL,
  decided_at DATETIME NULL,
  decision_note VARCHAR(512) NULL,
  expires_at DATETIME NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);--'录像调阅申请';

CREATE TABLE IF NOT EXISTS fire_video_access_log (
  id BIGINT NOT NULL PRIMARY KEY,
  org_id BIGINT NOT NULL,
  request_id BIGINT NOT NULL,
  recording_id BIGINT NULL,
  action VARCHAR(32) NOT NULL,
  staff_id BIGINT NULL,
  staff_name VARCHAR(64) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);--'录像调阅审计日志';

CREATE TABLE IF NOT EXISTS fire_training_drill (
  id BIGINT NOT NULL PRIMARY KEY,
  org_id BIGINT NOT NULL,
  drill_type VARCHAR(16) NOT NULL,
  topic VARCHAR(128) NOT NULL,
  planned_date DATE NULL,
  held_date DATE NULL,
  organizer_name VARCHAR(64) NULL,
  participant_count INT NULL,
  attendees VARCHAR(1024) NULL,
  summary VARCHAR(1024) NULL,
  next_plan VARCHAR(255) NULL,
  file_url VARCHAR(512) NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'PLANNED',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);--'消防培训与演练台账';

CREATE TABLE IF NOT EXISTS fire_safety_record (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  org_id BIGINT NOT NULL,
  record_type VARCHAR(32) NOT NULL,
  title VARCHAR(128) NOT NULL,
  location VARCHAR(128) NULL,
  inspector_name VARCHAR(64) NOT NULL,
  check_time DATETIME NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'OPEN',
  issue_description VARCHAR(1024) NULL,
  action_taken VARCHAR(1024) NULL,
  next_check_date DATE NULL,
  daily_reminder_enabled TINYINT NOT NULL DEFAULT 0,
  daily_reminder_time TIME NULL,
  source_record_id BIGINT NULL,
  check_round INT NOT NULL DEFAULT 1,
  qr_token VARCHAR(64) NULL,
  qr_generated_at DATETIME NULL,
  scan_completed_at DATETIME NULL,
  duty_record VARCHAR(1024) NULL,
  handover_punch_time DATETIME NULL,
  equipment_batch_no VARCHAR(128) NULL,
  product_production_date DATE NULL,
  product_expiry_date DATE NULL,
  check_cycle_days INT NULL,
  equipment_update_note VARCHAR(1024) NULL,
  equipment_aging_disposal VARCHAR(1024) NULL,
  image_urls TEXT NULL,
  third_party_maintenance_file_url VARCHAR(512) NULL,
  purchase_contract_file_url VARCHAR(512) NULL,
  contract_start_date DATE NULL,
  contract_end_date DATE NULL,
  purchase_document_urls TEXT NULL,
  created_by BIGINT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);
