SET @has_oa_knowledge_table := (
  SELECT COUNT(1)
  FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'oa_knowledge'
);

SET @knowledge_attachment_type_len := (
  SELECT COALESCE(CHARACTER_MAXIMUM_LENGTH, 0)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'oa_knowledge'
    AND COLUMN_NAME = 'attachment_type'
  LIMIT 1
);
SET @ddl_expand_knowledge_attachment_type := IF(
  @has_oa_knowledge_table = 1 AND @knowledge_attachment_type_len > 0 AND @knowledge_attachment_type_len < 128,
  'ALTER TABLE oa_knowledge MODIFY COLUMN attachment_type VARCHAR(128) DEFAULT NULL COMMENT ''附件类型''',
  'SELECT 1'
);
PREPARE stmt_expand_knowledge_attachment_type FROM @ddl_expand_knowledge_attachment_type;
EXECUTE stmt_expand_knowledge_attachment_type;
DEALLOCATE PREPARE stmt_expand_knowledge_attachment_type;

SET @knowledge_attachment_url_len := (
  SELECT COALESCE(CHARACTER_MAXIMUM_LENGTH, 0)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'oa_knowledge'
    AND COLUMN_NAME = 'attachment_url'
  LIMIT 1
);
SET @ddl_expand_knowledge_attachment_url := IF(
  @has_oa_knowledge_table = 1 AND @knowledge_attachment_url_len > 0 AND @knowledge_attachment_url_len < 1024,
  'ALTER TABLE oa_knowledge MODIFY COLUMN attachment_url VARCHAR(1024) DEFAULT NULL COMMENT ''附件地址''',
  'SELECT 1'
);
PREPARE stmt_expand_knowledge_attachment_url FROM @ddl_expand_knowledge_attachment_url;
EXECUTE stmt_expand_knowledge_attachment_url;
DEALLOCATE PREPARE stmt_expand_knowledge_attachment_url;

SET @has_oa_document_table := (
  SELECT COUNT(1)
  FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'oa_document'
);

SET @oa_document_url_len := (
  SELECT COALESCE(CHARACTER_MAXIMUM_LENGTH, 0)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'oa_document'
    AND COLUMN_NAME = 'url'
  LIMIT 1
);
SET @ddl_expand_oa_document_url := IF(
  @has_oa_document_table = 1 AND @oa_document_url_len > 0 AND @oa_document_url_len < 1024,
  'ALTER TABLE oa_document MODIFY COLUMN url VARCHAR(1024) DEFAULT NULL COMMENT ''存储地址''',
  'SELECT 1'
);
PREPARE stmt_expand_oa_document_url FROM @ddl_expand_oa_document_url;
EXECUTE stmt_expand_oa_document_url;
DEALLOCATE PREPARE stmt_expand_oa_document_url;
