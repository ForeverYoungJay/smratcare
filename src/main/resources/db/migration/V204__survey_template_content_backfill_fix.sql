SET @has_survey_template_table := (
  SELECT COUNT(1)
  FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'survey_template'
);
SET @has_survey_template_content := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'survey_template'
    AND COLUMN_NAME = 'content'
);
SET @has_survey_template_description := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'survey_template'
    AND COLUMN_NAME = 'description'
);
SET @ddl_update_survey_template_content := IF(
  @has_survey_template_table = 1
    AND @has_survey_template_content = 1
    AND @has_survey_template_description = 1,
  'UPDATE survey_template SET content = description WHERE content IS NULL AND description IS NOT NULL AND description <> ''''',
  'SELECT 1'
);
PREPARE stmt_update_survey_template_content FROM @ddl_update_survey_template_content;
EXECUTE stmt_update_survey_template_content;
DEALLOCATE PREPARE stmt_update_survey_template_content;
