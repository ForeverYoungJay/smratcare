SET @elder_source_type_exists = (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder'
    AND COLUMN_NAME = 'source_type'
);
SET @elder_source_type_sql = IF(
  @elder_source_type_exists = 0,
  'ALTER TABLE elder ADD COLUMN source_type VARCHAR(32) NULL COMMENT ''档案来源类型'' AFTER remark',
  'SELECT 1'
);
PREPARE stmt FROM @elder_source_type_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @elder_historical_contract_file_url_exists = (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder'
    AND COLUMN_NAME = 'historical_contract_file_url'
);
SET @elder_historical_contract_file_url_sql = IF(
  @elder_historical_contract_file_url_exists = 0,
  'ALTER TABLE elder ADD COLUMN historical_contract_file_url VARCHAR(500) NULL COMMENT ''历史合同附件地址'' AFTER source_type',
  'SELECT 1'
);
PREPARE stmt FROM @elder_historical_contract_file_url_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @elder_admission_source_type_exists = (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder_admission'
    AND COLUMN_NAME = 'source_type'
);
SET @elder_admission_source_type_sql = IF(
  @elder_admission_source_type_exists = 0,
  'ALTER TABLE elder_admission ADD COLUMN source_type VARCHAR(32) NULL COMMENT ''入住来源类型'' AFTER remark',
  'SELECT 1'
);
PREPARE stmt FROM @elder_admission_source_type_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
