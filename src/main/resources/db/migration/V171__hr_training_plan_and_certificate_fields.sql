SET @staff_training_record_source_training_id_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'staff_training_record'
    AND COLUMN_NAME = 'source_training_id'
);
SET @staff_training_record_source_training_id_sql = IF(
  @staff_training_record_source_training_id_exists = 0,
  'ALTER TABLE staff_training_record ADD COLUMN source_training_id BIGINT DEFAULT NULL COMMENT ''来源培训记录ID'' AFTER staff_id',
  'SELECT 1'
);
PREPARE stmt FROM @staff_training_record_source_training_id_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @staff_training_record_training_scene_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'staff_training_record'
    AND COLUMN_NAME = 'training_scene'
);
SET @staff_training_record_training_scene_sql = IF(
  @staff_training_record_training_scene_exists = 0,
  'ALTER TABLE staff_training_record ADD COLUMN training_scene VARCHAR(16) NOT NULL DEFAULT ''RECORD'' COMMENT ''培训场景 PLAN/RECORD/CERTIFICATE'' AFTER source_training_id',
  'SELECT 1'
);
PREPARE stmt FROM @staff_training_record_training_scene_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @staff_training_record_training_year_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'staff_training_record'
    AND COLUMN_NAME = 'training_year'
);
SET @staff_training_record_training_year_sql = IF(
  @staff_training_record_training_year_exists = 0,
  'ALTER TABLE staff_training_record ADD COLUMN training_year INT DEFAULT NULL COMMENT ''培训年度'' AFTER training_scene',
  'SELECT 1'
);
PREPARE stmt FROM @staff_training_record_training_year_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @staff_training_record_department_id_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'staff_training_record'
    AND COLUMN_NAME = 'department_id'
);
SET @staff_training_record_department_id_sql = IF(
  @staff_training_record_department_id_exists = 0,
  'ALTER TABLE staff_training_record ADD COLUMN department_id BIGINT DEFAULT NULL COMMENT ''部门ID'' AFTER training_year',
  'SELECT 1'
);
PREPARE stmt FROM @staff_training_record_department_id_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @staff_training_record_department_name_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'staff_training_record'
    AND COLUMN_NAME = 'department_name'
);
SET @staff_training_record_department_name_sql = IF(
  @staff_training_record_department_name_exists = 0,
  'ALTER TABLE staff_training_record ADD COLUMN department_name VARCHAR(64) DEFAULT NULL COMMENT ''部门名称快照'' AFTER department_id',
  'SELECT 1'
);
PREPARE stmt FROM @staff_training_record_department_name_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @staff_training_record_staff_no_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'staff_training_record'
    AND COLUMN_NAME = 'staff_no'
);
SET @staff_training_record_staff_no_sql = IF(
  @staff_training_record_staff_no_exists = 0,
  'ALTER TABLE staff_training_record ADD COLUMN staff_no VARCHAR(64) DEFAULT NULL COMMENT ''工号快照'' AFTER staff_id',
  'SELECT 1'
);
PREPARE stmt FROM @staff_training_record_staff_no_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @staff_training_record_attendance_status_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'staff_training_record'
    AND COLUMN_NAME = 'attendance_status'
);
SET @staff_training_record_attendance_status_sql = IF(
  @staff_training_record_attendance_status_exists = 0,
  'ALTER TABLE staff_training_record ADD COLUMN attendance_status VARCHAR(16) DEFAULT NULL COMMENT ''出勤状态 PRESENT/LATE/ABSENT/LEAVE'' AFTER score',
  'SELECT 1'
);
PREPARE stmt FROM @staff_training_record_attendance_status_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @staff_training_record_test_result_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'staff_training_record'
    AND COLUMN_NAME = 'test_result'
);
SET @staff_training_record_test_result_sql = IF(
  @staff_training_record_test_result_exists = 0,
  'ALTER TABLE staff_training_record ADD COLUMN test_result VARCHAR(64) DEFAULT NULL COMMENT ''测试结果 PASS/FAIL/EXCELLENT 或文本'' AFTER attendance_status',
  'SELECT 1'
);
PREPARE stmt FROM @staff_training_record_test_result_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @staff_training_record_certificate_required_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'staff_training_record'
    AND COLUMN_NAME = 'certificate_required'
);
SET @staff_training_record_certificate_required_sql = IF(
  @staff_training_record_certificate_required_exists = 0,
  'ALTER TABLE staff_training_record ADD COLUMN certificate_required TINYINT NOT NULL DEFAULT 0 COMMENT ''是否要求考证/发证'' AFTER test_result',
  'SELECT 1'
);
PREPARE stmt FROM @staff_training_record_certificate_required_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @staff_training_record_certificate_status_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'staff_training_record'
    AND COLUMN_NAME = 'certificate_status'
);
SET @staff_training_record_certificate_status_sql = IF(
  @staff_training_record_certificate_status_exists = 0,
  'ALTER TABLE staff_training_record ADD COLUMN certificate_status VARCHAR(16) DEFAULT NULL COMMENT ''证书状态 NONE/PENDING/GENERATED/ISSUED'' AFTER certificate_required',
  'SELECT 1'
);
PREPARE stmt FROM @staff_training_record_certificate_status_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @staff_training_record_instructor_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'staff_training_record'
    AND COLUMN_NAME = 'instructor'
);
SET @staff_training_record_instructor_sql = IF(
  @staff_training_record_instructor_exists = 0,
  'ALTER TABLE staff_training_record ADD COLUMN instructor VARCHAR(64) DEFAULT NULL COMMENT ''讲师'' AFTER provider',
  'SELECT 1'
);
PREPARE stmt FROM @staff_training_record_instructor_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @staff_training_record_attachments_json_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'staff_training_record'
    AND COLUMN_NAME = 'attachments_json'
);
SET @staff_training_record_attachments_json_sql = IF(
  @staff_training_record_attachments_json_exists = 0,
  'ALTER TABLE staff_training_record ADD COLUMN attachments_json TEXT DEFAULT NULL COMMENT ''附件JSON'' AFTER certificate_no',
  'SELECT 1'
);
PREPARE stmt FROM @staff_training_record_attachments_json_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE staff_training_record
SET training_scene = CASE
    WHEN training_type = 'CERTIFICATE' THEN 'CERTIFICATE'
    ELSE 'RECORD'
  END
WHERE training_scene IS NULL OR training_scene = '';
