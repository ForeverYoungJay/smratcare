-- 消防安全：楼栋点位与检查模板（管理员配置）、控制室交接双方确认、维保报告签署、
-- 录像调阅申请与审计、消防培训演练台账

CREATE TABLE IF NOT EXISTS fire_location (
  id BIGINT NOT NULL PRIMARY KEY,
  org_id BIGINT NOT NULL,
  building VARCHAR(64) NOT NULL COMMENT '楼栋',
  floor_name VARCHAR(32) NULL COMMENT '楼层',
  area VARCHAR(64) NULL COMMENT '区域/部位',
  point_code VARCHAR(64) NULL COMMENT '巡查点位编号（对应现场二维码）',
  risk_level VARCHAR(16) NOT NULL DEFAULT 'MEDIUM' COMMENT 'HIGH/MEDIUM/LOW',
  key_part TINYINT NOT NULL DEFAULT 0 COMMENT '重点部位（居室/公共活动用房/厨房等）',
  enabled TINYINT NOT NULL DEFAULT 1,
  remark VARCHAR(255) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_fire_location_org (org_id, building)
) COMMENT='消防楼栋与巡查点位';

CREATE TABLE IF NOT EXISTS fire_check_template (
  id BIGINT NOT NULL PRIMARY KEY,
  org_id BIGINT NOT NULL,
  record_type VARCHAR(32) NOT NULL COMMENT '适用记录类型 DAY_PATROL/NIGHT_PATROL/MONTHLY_CHECK',
  template_name VARCHAR(64) NOT NULL,
  items TEXT NOT NULL COMMENT '检查项，一行一项',
  version INT NOT NULL DEFAULT 1,
  enabled TINYINT NOT NULL DEFAULT 1,
  updated_by_name VARCHAR(64) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_fire_template_org (org_id, record_type)
) COMMENT='消防检查模板';

CREATE TABLE IF NOT EXISTS fire_duty_handover (
  id BIGINT NOT NULL PRIMARY KEY,
  org_id BIGINT NOT NULL,
  shift_date DATE NOT NULL,
  shift_name VARCHAR(32) NOT NULL,
  handover_staff_id BIGINT NOT NULL,
  handover_name VARCHAR(64) NULL,
  takeover_staff_id BIGINT NOT NULL,
  takeover_name VARCHAR(64) NULL,
  checklist TEXT NULL COMMENT '交接清单（JSON 数组：item/checked）',
  open_issues VARCHAR(1024) NULL COMMENT '未关闭故障与待处理事项',
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/CONFIRMED/REJECTED',
  handover_at DATETIME NOT NULL,
  takeover_at DATETIME NULL,
  takeover_note VARCHAR(512) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_fire_handover_org (org_id, shift_date)
) COMMENT='消防控制室交接班（交班、接班双方确认）';

CREATE TABLE IF NOT EXISTS fire_report_signature (
  id BIGINT NOT NULL PRIMARY KEY,
  org_id BIGINT NOT NULL,
  record_id BIGINT NOT NULL COMMENT 'fire_safety_record（维保报告）',
  sign_type VARCHAR(16) NOT NULL COMMENT 'SUBMIT 提交签署 / REVIEW 审核签署',
  signer_id BIGINT NOT NULL,
  signer_name VARCHAR(64) NULL,
  file_url VARCHAR(512) NULL,
  file_sha256 VARCHAR(64) NULL COMMENT '签署时服务端计算的文件哈希；存储不支持时为空',
  comment VARCHAR(512) NULL,
  signed_at DATETIME NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_fire_sign_record (org_id, record_id)
) COMMENT='维保报告签署记录（只增不改）';

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
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
  approver_id BIGINT NULL,
  approver_name VARCHAR(64) NULL,
  decided_at DATETIME NULL,
  decision_note VARCHAR(512) NULL,
  expires_at DATETIME NULL COMMENT '批准后可查看截止时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_fire_video_req_org (org_id, status)
) COMMENT='录像调阅申请';

CREATE TABLE IF NOT EXISTS fire_video_access_log (
  id BIGINT NOT NULL PRIMARY KEY,
  org_id BIGINT NOT NULL,
  request_id BIGINT NOT NULL,
  recording_id BIGINT NULL,
  action VARCHAR(32) NOT NULL COMMENT 'REQUEST/APPROVE/REJECT/LIST/VIEW',
  staff_id BIGINT NULL,
  staff_name VARCHAR(64) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_fire_video_log_req (org_id, request_id)
) COMMENT='录像调阅审计日志';

CREATE TABLE IF NOT EXISTS fire_training_drill (
  id BIGINT NOT NULL PRIMARY KEY,
  org_id BIGINT NOT NULL,
  drill_type VARCHAR(16) NOT NULL COMMENT 'DRILL 演练 / TRAINING 培训',
  topic VARCHAR(128) NOT NULL,
  planned_date DATE NULL,
  held_date DATE NULL,
  organizer_name VARCHAR(64) NULL,
  participant_count INT NULL,
  attendees VARCHAR(1024) NULL COMMENT '签到人员',
  summary VARCHAR(1024) NULL COMMENT '总结与复盘',
  next_plan VARCHAR(255) NULL,
  file_url VARCHAR(512) NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'PLANNED' COMMENT 'PLANNED/DONE',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_fire_drill_org (org_id, drill_type)
) COMMENT='消防培训与演练台账';
