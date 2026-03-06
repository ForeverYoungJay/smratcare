SET @has_oa_document_folder_table := (
  SELECT COUNT(1)
  FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'oa_document_folder'
);

SET @has_visibility := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'oa_document_folder'
    AND COLUMN_NAME = 'visibility'
);
SET @ddl_add_visibility := IF(
  @has_oa_document_folder_table = 1 AND @has_visibility = 0,
  'ALTER TABLE oa_document_folder ADD COLUMN visibility VARCHAR(16) NOT NULL DEFAULT ''PUBLIC'' COMMENT ''PUBLIC/PRIVATE'' AFTER status',
  'SELECT 1'
);
PREPARE stmt_add_visibility FROM @ddl_add_visibility;
EXECUTE stmt_add_visibility;
DEALLOCATE PREPARE stmt_add_visibility;

SET @has_region_code := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'oa_document_folder'
    AND COLUMN_NAME = 'region_code'
);
SET @ddl_add_region_code := IF(
  @has_oa_document_folder_table = 1 AND @has_region_code = 0,
  'ALTER TABLE oa_document_folder ADD COLUMN region_code VARCHAR(64) DEFAULT NULL COMMENT ''区域编码'' AFTER visibility',
  'SELECT 1'
);
PREPARE stmt_add_region_code FROM @ddl_add_region_code;
EXECUTE stmt_add_region_code;
DEALLOCATE PREPARE stmt_add_region_code;
