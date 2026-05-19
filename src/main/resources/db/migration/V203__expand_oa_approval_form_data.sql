SET @oa_approval_form_data_type = (
  SELECT COALESCE(DATA_TYPE, '')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'oa_approval'
    AND COLUMN_NAME = 'form_data'
  LIMIT 1
);

SET @ddl = IF(
  @oa_approval_form_data_type <> ''
    AND @oa_approval_form_data_type NOT IN ('text', 'mediumtext', 'longtext'),
  'ALTER TABLE oa_approval MODIFY COLUMN form_data LONGTEXT DEFAULT NULL COMMENT ''表单数据(JSON)''',
  'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
