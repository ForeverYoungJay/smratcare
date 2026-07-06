-- 抢救记录单（时间线明细 JSON、参与人员、用药抢救措施、结果）
CREATE TABLE IF NOT EXISTS medical_rescue_record (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NULL,
  org_id BIGINT NOT NULL,
  event_id BIGINT NOT NULL,
  elder_id BIGINT NULL,
  timeline_json TEXT NULL,
  participants VARCHAR(500) NULL,
  drugs_used VARCHAR(500) NULL,
  measures VARCHAR(1000) NULL,
  result VARCHAR(16) NULL,
  result_note VARCHAR(500) NULL,
  start_time DATETIME NULL,
  end_time DATETIME NULL,
  recorder_id BIGINT NULL,
  recorder_name VARCHAR(60) NULL,
  remark VARCHAR(500) NULL,
  created_by BIGINT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_rescue_record_event (org_id, event_id)
);
