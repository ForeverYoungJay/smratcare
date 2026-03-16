SET @staff_profile_contract_no_exists = (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'staff_profile'
    AND COLUMN_NAME = 'contract_no'
);
SET @staff_profile_contract_no_sql = IF(
  @staff_profile_contract_no_exists = 0,
  'ALTER TABLE staff_profile ADD COLUMN contract_no VARCHAR(64) DEFAULT NULL COMMENT ''劳动合同编号'' AFTER employment_type',
  'SELECT 1'
);
PREPARE stmt FROM @staff_profile_contract_no_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @staff_profile_contract_type_exists = (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'staff_profile'
    AND COLUMN_NAME = 'contract_type'
);
SET @staff_profile_contract_type_sql = IF(
  @staff_profile_contract_type_exists = 0,
  'ALTER TABLE staff_profile ADD COLUMN contract_type VARCHAR(32) DEFAULT NULL COMMENT ''合同类型'' AFTER contract_no',
  'SELECT 1'
);
PREPARE stmt FROM @staff_profile_contract_type_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @staff_profile_contract_status_exists = (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'staff_profile'
    AND COLUMN_NAME = 'contract_status'
);
SET @staff_profile_contract_status_sql = IF(
  @staff_profile_contract_status_exists = 0,
  'ALTER TABLE staff_profile ADD COLUMN contract_status VARCHAR(32) DEFAULT NULL COMMENT ''合同状态'' AFTER contract_type',
  'SELECT 1'
);
PREPARE stmt FROM @staff_profile_contract_status_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @staff_profile_contract_start_date_exists = (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'staff_profile'
    AND COLUMN_NAME = 'contract_start_date'
);
SET @staff_profile_contract_start_date_sql = IF(
  @staff_profile_contract_start_date_exists = 0,
  'ALTER TABLE staff_profile ADD COLUMN contract_start_date DATE DEFAULT NULL COMMENT ''合同开始日期'' AFTER contract_status',
  'SELECT 1'
);
PREPARE stmt FROM @staff_profile_contract_start_date_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @staff_profile_contract_end_date_exists = (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'staff_profile'
    AND COLUMN_NAME = 'contract_end_date'
);
SET @staff_profile_contract_end_date_sql = IF(
  @staff_profile_contract_end_date_exists = 0,
  'ALTER TABLE staff_profile ADD COLUMN contract_end_date DATE DEFAULT NULL COMMENT ''合同结束日期'' AFTER contract_start_date',
  'SELECT 1'
);
PREPARE stmt FROM @staff_profile_contract_end_date_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
