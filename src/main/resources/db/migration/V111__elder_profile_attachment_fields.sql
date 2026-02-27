-- 老人档案补充附件字段：医保卡复印件 / 户口材料复印件 / 病历文件

SET @has_medical_insurance_copy_url := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder'
    AND COLUMN_NAME = 'medical_insurance_copy_url'
);
SET @sql_medical_insurance_copy_url := IF(
  @has_medical_insurance_copy_url = 0,
  'ALTER TABLE elder ADD COLUMN medical_insurance_copy_url VARCHAR(512) DEFAULT NULL COMMENT ''医保卡复印件URL'' AFTER home_address',
  'SELECT 1'
);
PREPARE stmt FROM @sql_medical_insurance_copy_url;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_household_copy_url := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder'
    AND COLUMN_NAME = 'household_copy_url'
);
SET @sql_household_copy_url := IF(
  @has_household_copy_url = 0,
  'ALTER TABLE elder ADD COLUMN household_copy_url VARCHAR(512) DEFAULT NULL COMMENT ''户口材料复印件URL'' AFTER medical_insurance_copy_url',
  'SELECT 1'
);
PREPARE stmt FROM @sql_household_copy_url;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_medical_record_file_url := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder'
    AND COLUMN_NAME = 'medical_record_file_url'
);
SET @sql_medical_record_file_url := IF(
  @has_medical_record_file_url = 0,
  'ALTER TABLE elder ADD COLUMN medical_record_file_url VARCHAR(512) DEFAULT NULL COMMENT ''病历文件URL'' AFTER household_copy_url',
  'SELECT 1'
);
PREPARE stmt FROM @sql_medical_record_file_url;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
