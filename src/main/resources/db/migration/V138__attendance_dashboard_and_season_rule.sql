CREATE TABLE IF NOT EXISTS attendance_season_rule (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  winter_work_start VARCHAR(8) NOT NULL DEFAULT '13:30' COMMENT '冬季上班时间',
  winter_work_end VARCHAR(8) NOT NULL DEFAULT '17:00' COMMENT '冬季下班时间',
  summer_work_start VARCHAR(8) NOT NULL DEFAULT '14:30' COMMENT '夏季上班时间',
  summer_work_end VARCHAR(8) NOT NULL DEFAULT '18:00' COMMENT '夏季下班时间',
  late_grace_minutes INT NOT NULL DEFAULT 10 COMMENT '迟到宽限分钟',
  early_leave_grace_minutes INT NOT NULL DEFAULT 10 COMMENT '早退宽限分钟',
  outing_max_minutes INT NOT NULL DEFAULT 180 COMMENT '外出超时阈值分钟',
  enabled_status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_attendance_season_rule_org (org_id, is_deleted)
) COMMENT='考勤季节规则';

SET @has_work_date = (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'attendance_record' AND column_name = 'work_date'
);
SET @sql_work_date = IF(@has_work_date = 0,
  'ALTER TABLE attendance_record ADD COLUMN work_date DATE NULL COMMENT ''工作日期'' AFTER staff_id',
  'SELECT 1');
PREPARE stmt_work_date FROM @sql_work_date;
EXECUTE stmt_work_date;
DEALLOCATE PREPARE stmt_work_date;

SET @has_outing_start = (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'attendance_record' AND column_name = 'outing_start_time'
);
SET @sql_outing_start = IF(@has_outing_start = 0,
  'ALTER TABLE attendance_record ADD COLUMN outing_start_time DATETIME NULL COMMENT ''外出开始时间'' AFTER check_out_time',
  'SELECT 1');
PREPARE stmt_outing_start FROM @sql_outing_start;
EXECUTE stmt_outing_start;
DEALLOCATE PREPARE stmt_outing_start;

SET @has_outing_end = (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'attendance_record' AND column_name = 'outing_end_time'
);
SET @sql_outing_end = IF(@has_outing_end = 0,
  'ALTER TABLE attendance_record ADD COLUMN outing_end_time DATETIME NULL COMMENT ''外出结束时间'' AFTER outing_start_time',
  'SELECT 1');
PREPARE stmt_outing_end FROM @sql_outing_end;
EXECUTE stmt_outing_end;
DEALLOCATE PREPARE stmt_outing_end;

SET @has_lunch_start = (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'attendance_record' AND column_name = 'lunch_break_start_time'
);
SET @sql_lunch_start = IF(@has_lunch_start = 0,
  'ALTER TABLE attendance_record ADD COLUMN lunch_break_start_time DATETIME NULL COMMENT ''午休开始'' AFTER outing_end_time',
  'SELECT 1');
PREPARE stmt_lunch_start FROM @sql_lunch_start;
EXECUTE stmt_lunch_start;
DEALLOCATE PREPARE stmt_lunch_start;

SET @has_lunch_end = (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'attendance_record' AND column_name = 'lunch_break_end_time'
);
SET @sql_lunch_end = IF(@has_lunch_end = 0,
  'ALTER TABLE attendance_record ADD COLUMN lunch_break_end_time DATETIME NULL COMMENT ''午休结束'' AFTER lunch_break_start_time',
  'SELECT 1');
PREPARE stmt_lunch_end FROM @sql_lunch_end;
EXECUTE stmt_lunch_end;
DEALLOCATE PREPARE stmt_lunch_end;

SET @has_outing_minutes = (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'attendance_record' AND column_name = 'outing_minutes'
);
SET @sql_outing_minutes = IF(@has_outing_minutes = 0,
  'ALTER TABLE attendance_record ADD COLUMN outing_minutes INT NOT NULL DEFAULT 0 COMMENT ''外出时长(分钟)'' AFTER lunch_break_end_time',
  'SELECT 1');
PREPARE stmt_outing_minutes FROM @sql_outing_minutes;
EXECUTE stmt_outing_minutes;
DEALLOCATE PREPARE stmt_outing_minutes;

SET @has_note = (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'attendance_record' AND column_name = 'note'
);
SET @sql_note = IF(@has_note = 0,
  'ALTER TABLE attendance_record ADD COLUMN note VARCHAR(255) NULL COMMENT ''备注'' AFTER status',
  'SELECT 1');
PREPARE stmt_note FROM @sql_note;
EXECUTE stmt_note;
DEALLOCATE PREPARE stmt_note;

UPDATE attendance_record
SET work_date = DATE(check_in_time)
WHERE work_date IS NULL AND check_in_time IS NOT NULL;

SET @has_idx_work_date = (
  SELECT COUNT(*) FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'attendance_record' AND index_name = 'idx_attendance_org_staff_work_date'
);
SET @sql_idx_work_date = IF(@has_idx_work_date = 0,
  'CREATE INDEX idx_attendance_org_staff_work_date ON attendance_record (org_id, staff_id, work_date)',
  'SELECT 1');
PREPARE stmt_idx_work_date FROM @sql_idx_work_date;
EXECUTE stmt_idx_work_date;
DEALLOCATE PREPARE stmt_idx_work_date;
