SET @has_oa_album_table := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.TABLES
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'oa_album'
);

SET @has_oa_album_scope := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'oa_album'
    AND COLUMN_NAME = 'album_scope'
);

SET @ddl_add_oa_album_scope := IF(
  @has_oa_album_table = 1 AND @has_oa_album_scope = 0,
  'ALTER TABLE oa_album ADD COLUMN album_scope VARCHAR(16) NOT NULL DEFAULT ''GROUP'' COMMENT ''相册范围 PERSONAL/GROUP'' AFTER folder_name',
  'SELECT 1'
);
PREPARE stmt_add_oa_album_scope FROM @ddl_add_oa_album_scope;
EXECUTE stmt_add_oa_album_scope;
DEALLOCATE PREPARE stmt_add_oa_album_scope;

SET @has_oa_album_elder_id := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'oa_album'
    AND COLUMN_NAME = 'elder_id'
);

SET @ddl_add_oa_album_elder_id := IF(
  @has_oa_album_table = 1 AND @has_oa_album_elder_id = 0,
  'ALTER TABLE oa_album ADD COLUMN elder_id BIGINT DEFAULT NULL COMMENT ''个人相册所属老人ID'' AFTER album_scope',
  'SELECT 1'
);
PREPARE stmt_add_oa_album_elder_id FROM @ddl_add_oa_album_elder_id;
EXECUTE stmt_add_oa_album_elder_id;
DEALLOCATE PREPARE stmt_add_oa_album_elder_id;

SET @has_oa_album_scope_idx := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'oa_album'
    AND INDEX_NAME = 'idx_oa_album_org_scope_elder'
);

SET @ddl_add_oa_album_scope_idx := IF(
  @has_oa_album_table = 1 AND @has_oa_album_scope_idx = 0,
  'ALTER TABLE oa_album ADD INDEX idx_oa_album_org_scope_elder (org_id, album_scope, elder_id, status)',
  'SELECT 1'
);
PREPARE stmt_add_oa_album_scope_idx FROM @ddl_add_oa_album_scope_idx;
EXECUTE stmt_add_oa_album_scope_idx;
DEALLOCATE PREPARE stmt_add_oa_album_scope_idx;
