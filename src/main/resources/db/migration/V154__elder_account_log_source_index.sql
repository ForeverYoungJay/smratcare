SET @idx_elder_account_log_source_exists := (
  SELECT COUNT(1)
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder_account_log'
    AND INDEX_NAME = 'idx_elder_account_log_source'
);
SET @idx_elder_account_log_source_sql := IF(
  @idx_elder_account_log_source_exists = 0,
  'ALTER TABLE elder_account_log ADD INDEX idx_elder_account_log_source (org_id, source_type, source_id, is_deleted)',
  'SELECT 1'
);
PREPARE stmt FROM @idx_elder_account_log_source_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
