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
SET @ddl_add_survey_template_content := IF(
  @has_survey_template_table = 1 AND @has_survey_template_content = 0,
  'ALTER TABLE survey_template ADD COLUMN content TEXT DEFAULT NULL COMMENT ''问卷内容说明'' AFTER description',
  'SELECT 1'
);
PREPARE stmt_add_survey_template_content FROM @ddl_add_survey_template_content;
EXECUTE stmt_add_survey_template_content;
DEALLOCATE PREPARE stmt_add_survey_template_content;

SET @ddl_update_survey_template_content := IF(
  @has_survey_template_table = 1,
  'UPDATE survey_template SET content = description WHERE content IS NULL AND description IS NOT NULL AND description <> ''''''',
  'SELECT 1'
);
PREPARE stmt_update_survey_template_content FROM @ddl_update_survey_template_content;
EXECUTE stmt_update_survey_template_content;
DEALLOCATE PREPARE stmt_update_survey_template_content;

SET @has_oa_album_table := (
  SELECT COUNT(1)
  FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'oa_album'
);
SET @has_oa_album_folder_name := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'oa_album'
    AND COLUMN_NAME = 'folder_name'
);
SET @ddl_add_oa_album_folder_name := IF(
  @has_oa_album_table = 1 AND @has_oa_album_folder_name = 0,
  'ALTER TABLE oa_album ADD COLUMN folder_name VARCHAR(128) DEFAULT NULL COMMENT ''照片夹名称'' AFTER category',
  'SELECT 1'
);
PREPARE stmt_add_oa_album_folder_name FROM @ddl_add_oa_album_folder_name;
EXECUTE stmt_add_oa_album_folder_name;
DEALLOCATE PREPARE stmt_add_oa_album_folder_name;

SET @has_oa_album_photos_json := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'oa_album'
    AND COLUMN_NAME = 'photos_json'
);
SET @ddl_add_oa_album_photos_json := IF(
  @has_oa_album_table = 1 AND @has_oa_album_photos_json = 0,
  'ALTER TABLE oa_album ADD COLUMN photos_json TEXT DEFAULT NULL COMMENT ''照片列表JSON'' AFTER cover_url',
  'SELECT 1'
);
PREPARE stmt_add_oa_album_photos_json FROM @ddl_add_oa_album_photos_json;
EXECUTE stmt_add_oa_album_photos_json;
DEALLOCATE PREPARE stmt_add_oa_album_photos_json;

SET @has_oa_knowledge_table := (
  SELECT COUNT(1)
  FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'oa_knowledge'
);
SET @has_oa_knowledge_attachment_name := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'oa_knowledge'
    AND COLUMN_NAME = 'attachment_name'
);
SET @ddl_add_oa_knowledge_attachment_name := IF(
  @has_oa_knowledge_table = 1 AND @has_oa_knowledge_attachment_name = 0,
  'ALTER TABLE oa_knowledge ADD COLUMN attachment_name VARCHAR(255) DEFAULT NULL COMMENT ''附件名称'' AFTER content',
  'SELECT 1'
);
PREPARE stmt_add_oa_knowledge_attachment_name FROM @ddl_add_oa_knowledge_attachment_name;
EXECUTE stmt_add_oa_knowledge_attachment_name;
DEALLOCATE PREPARE stmt_add_oa_knowledge_attachment_name;

SET @has_oa_knowledge_attachment_url := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'oa_knowledge'
    AND COLUMN_NAME = 'attachment_url'
);
SET @ddl_add_oa_knowledge_attachment_url := IF(
  @has_oa_knowledge_table = 1 AND @has_oa_knowledge_attachment_url = 0,
  'ALTER TABLE oa_knowledge ADD COLUMN attachment_url VARCHAR(512) DEFAULT NULL COMMENT ''附件地址'' AFTER attachment_name',
  'SELECT 1'
);
PREPARE stmt_add_oa_knowledge_attachment_url FROM @ddl_add_oa_knowledge_attachment_url;
EXECUTE stmt_add_oa_knowledge_attachment_url;
DEALLOCATE PREPARE stmt_add_oa_knowledge_attachment_url;

SET @has_oa_knowledge_attachment_type := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'oa_knowledge'
    AND COLUMN_NAME = 'attachment_type'
);
SET @ddl_add_oa_knowledge_attachment_type := IF(
  @has_oa_knowledge_table = 1 AND @has_oa_knowledge_attachment_type = 0,
  'ALTER TABLE oa_knowledge ADD COLUMN attachment_type VARCHAR(64) DEFAULT NULL COMMENT ''附件类型'' AFTER attachment_url',
  'SELECT 1'
);
PREPARE stmt_add_oa_knowledge_attachment_type FROM @ddl_add_oa_knowledge_attachment_type;
EXECUTE stmt_add_oa_knowledge_attachment_type;
DEALLOCATE PREPARE stmt_add_oa_knowledge_attachment_type;

SET @has_oa_knowledge_attachment_size := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'oa_knowledge'
    AND COLUMN_NAME = 'attachment_size'
);
SET @ddl_add_oa_knowledge_attachment_size := IF(
  @has_oa_knowledge_table = 1 AND @has_oa_knowledge_attachment_size = 0,
  'ALTER TABLE oa_knowledge ADD COLUMN attachment_size BIGINT DEFAULT NULL COMMENT ''附件大小(B)'' AFTER attachment_type',
  'SELECT 1'
);
PREPARE stmt_add_oa_knowledge_attachment_size FROM @ddl_add_oa_knowledge_attachment_size;
EXECUTE stmt_add_oa_knowledge_attachment_size;
DEALLOCATE PREPARE stmt_add_oa_knowledge_attachment_size;

SET @has_oa_knowledge_expired_at := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'oa_knowledge'
    AND COLUMN_NAME = 'expired_at'
);
SET @ddl_add_oa_knowledge_expired_at := IF(
  @has_oa_knowledge_table = 1 AND @has_oa_knowledge_expired_at = 0,
  'ALTER TABLE oa_knowledge ADD COLUMN expired_at DATETIME DEFAULT NULL COMMENT ''过期时间'' AFTER published_at',
  'SELECT 1'
);
PREPARE stmt_add_oa_knowledge_expired_at FROM @ddl_add_oa_knowledge_expired_at;
EXECUTE stmt_add_oa_knowledge_expired_at;
DEALLOCATE PREPARE stmt_add_oa_knowledge_expired_at;

SET @content_nullable := (
  SELECT IS_NULLABLE
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'oa_knowledge'
    AND COLUMN_NAME = 'content'
  LIMIT 1
);
SET @ddl_alter_knowledge_content := IF(
  @has_oa_knowledge_table = 1 AND @content_nullable = 'NO',
  'ALTER TABLE oa_knowledge MODIFY COLUMN content TEXT NULL COMMENT ''正文''',
  'SELECT 1'
);
PREPARE stmt_alter_knowledge_content FROM @ddl_alter_knowledge_content;
EXECUTE stmt_alter_knowledge_content;
DEALLOCATE PREPARE stmt_alter_knowledge_content;
