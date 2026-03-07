SET @has_late_enabled = (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'attendance_season_rule' AND column_name = 'late_enabled'
);
SET @sql_late_enabled = IF(@has_late_enabled = 0,
  'ALTER TABLE attendance_season_rule ADD COLUMN late_enabled TINYINT NOT NULL DEFAULT 1 COMMENT ''迟到判定开关'' AFTER outing_max_minutes',
  'SELECT 1');
PREPARE stmt_late_enabled FROM @sql_late_enabled;
EXECUTE stmt_late_enabled;
DEALLOCATE PREPARE stmt_late_enabled;

SET @has_early_leave_enabled = (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'attendance_season_rule' AND column_name = 'early_leave_enabled'
);
SET @sql_early_leave_enabled = IF(@has_early_leave_enabled = 0,
  'ALTER TABLE attendance_season_rule ADD COLUMN early_leave_enabled TINYINT NOT NULL DEFAULT 1 COMMENT ''早退判定开关'' AFTER late_enabled',
  'SELECT 1');
PREPARE stmt_early_leave_enabled FROM @sql_early_leave_enabled;
EXECUTE stmt_early_leave_enabled;
DEALLOCATE PREPARE stmt_early_leave_enabled;

SET @has_outing_overtime_enabled = (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'attendance_season_rule' AND column_name = 'outing_overtime_enabled'
);
SET @sql_outing_overtime_enabled = IF(@has_outing_overtime_enabled = 0,
  'ALTER TABLE attendance_season_rule ADD COLUMN outing_overtime_enabled TINYINT NOT NULL DEFAULT 1 COMMENT ''外出超时判定开关'' AFTER early_leave_enabled',
  'SELECT 1');
PREPARE stmt_outing_overtime_enabled FROM @sql_outing_overtime_enabled;
EXECUTE stmt_outing_overtime_enabled;
DEALLOCATE PREPARE stmt_outing_overtime_enabled;

SET @has_missing_checkout_enabled = (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'attendance_season_rule' AND column_name = 'missing_checkout_enabled'
);
SET @sql_missing_checkout_enabled = IF(@has_missing_checkout_enabled = 0,
  'ALTER TABLE attendance_season_rule ADD COLUMN missing_checkout_enabled TINYINT NOT NULL DEFAULT 1 COMMENT ''缺签退判定开关'' AFTER outing_overtime_enabled',
  'SELECT 1');
PREPARE stmt_missing_checkout_enabled FROM @sql_missing_checkout_enabled;
EXECUTE stmt_missing_checkout_enabled;
DEALLOCATE PREPARE stmt_missing_checkout_enabled;

SET @has_reviewed = (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'attendance_record' AND column_name = 'reviewed'
);
SET @sql_reviewed = IF(@has_reviewed = 0,
  'ALTER TABLE attendance_record ADD COLUMN reviewed TINYINT NOT NULL DEFAULT 0 COMMENT ''异常是否已核验'' AFTER outing_minutes',
  'SELECT 1');
PREPARE stmt_reviewed FROM @sql_reviewed;
EXECUTE stmt_reviewed;
DEALLOCATE PREPARE stmt_reviewed;

SET @has_reviewed_by = (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'attendance_record' AND column_name = 'reviewed_by'
);
SET @sql_reviewed_by = IF(@has_reviewed_by = 0,
  'ALTER TABLE attendance_record ADD COLUMN reviewed_by BIGINT NULL COMMENT ''核验人'' AFTER reviewed',
  'SELECT 1');
PREPARE stmt_reviewed_by FROM @sql_reviewed_by;
EXECUTE stmt_reviewed_by;
DEALLOCATE PREPARE stmt_reviewed_by;

SET @has_reviewed_at = (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'attendance_record' AND column_name = 'reviewed_at'
);
SET @sql_reviewed_at = IF(@has_reviewed_at = 0,
  'ALTER TABLE attendance_record ADD COLUMN reviewed_at DATETIME NULL COMMENT ''核验时间'' AFTER reviewed_by',
  'SELECT 1');
PREPARE stmt_reviewed_at FROM @sql_reviewed_at;
EXECUTE stmt_reviewed_at;
DEALLOCATE PREPARE stmt_reviewed_at;

SET @has_review_remark = (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'attendance_record' AND column_name = 'review_remark'
);
SET @sql_review_remark = IF(@has_review_remark = 0,
  'ALTER TABLE attendance_record ADD COLUMN review_remark VARCHAR(255) NULL COMMENT ''核验备注'' AFTER reviewed_at',
  'SELECT 1');
PREPARE stmt_review_remark FROM @sql_review_remark;
EXECUTE stmt_review_remark;
DEALLOCATE PREPARE stmt_review_remark;
