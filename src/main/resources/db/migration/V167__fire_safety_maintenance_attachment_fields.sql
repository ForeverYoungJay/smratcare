SET @fs_third_party_maintenance_file_url_exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'fire_safety_record'
    AND COLUMN_NAME = 'third_party_maintenance_file_url'
);
SET @fs_purchase_contract_file_url_exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'fire_safety_record'
    AND COLUMN_NAME = 'purchase_contract_file_url'
);
SET @fs_contract_start_date_exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'fire_safety_record'
    AND COLUMN_NAME = 'contract_start_date'
);
SET @fs_contract_end_date_exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'fire_safety_record'
    AND COLUMN_NAME = 'contract_end_date'
);
SET @fs_purchase_document_urls_exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'fire_safety_record'
    AND COLUMN_NAME = 'purchase_document_urls'
);

SET @sql = IF(
  @fs_third_party_maintenance_file_url_exists = 0,
  'ALTER TABLE fire_safety_record ADD COLUMN third_party_maintenance_file_url VARCHAR(512) NULL COMMENT ''第三方维保记录单'' AFTER image_urls',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
  @fs_purchase_contract_file_url_exists = 0,
  'ALTER TABLE fire_safety_record ADD COLUMN purchase_contract_file_url VARCHAR(512) NULL COMMENT ''采购合同附件'' AFTER third_party_maintenance_file_url',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
  @fs_contract_start_date_exists = 0,
  'ALTER TABLE fire_safety_record ADD COLUMN contract_start_date DATE NULL COMMENT ''合约开始日期'' AFTER purchase_contract_file_url',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
  @fs_contract_end_date_exists = 0,
  'ALTER TABLE fire_safety_record ADD COLUMN contract_end_date DATE NULL COMMENT ''合约结束日期'' AFTER contract_start_date',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
  @fs_purchase_document_urls_exists = 0,
  'ALTER TABLE fire_safety_record ADD COLUMN purchase_document_urls TEXT NULL COMMENT ''采购单据附件，按换行分隔'' AFTER contract_end_date',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
