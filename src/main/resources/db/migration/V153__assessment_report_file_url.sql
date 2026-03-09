SET @assessment_report_file_url_exists := (
  SELECT COUNT(1)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'assessment_record'
    AND COLUMN_NAME = 'report_file_url'
);
SET @assessment_report_file_url_sql := IF(
  @assessment_report_file_url_exists = 0,
  'ALTER TABLE assessment_record ADD COLUMN report_file_url VARCHAR(1024) DEFAULT NULL COMMENT ''评估报告文件URL'' AFTER source',
  'SELECT 1'
);
PREPARE stmt FROM @assessment_report_file_url_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @assessment_report_file_name_exists := (
  SELECT COUNT(1)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'assessment_record'
    AND COLUMN_NAME = 'report_file_name'
);
SET @assessment_report_file_name_sql := IF(
  @assessment_report_file_name_exists = 0,
  'ALTER TABLE assessment_record ADD COLUMN report_file_name VARCHAR(255) DEFAULT NULL COMMENT ''评估报告文件名'' AFTER report_file_url',
  'SELECT 1'
);
PREPARE stmt FROM @assessment_report_file_name_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
