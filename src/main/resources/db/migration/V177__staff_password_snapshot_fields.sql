SET @schema := DATABASE();

SET @sql := IF(
  EXISTS (
    SELECT 1
    FROM information_schema.tables
    WHERE table_schema = @schema
      AND table_name = 'staff'
  )
  AND NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = @schema
      AND table_name = 'staff'
      AND column_name = 'password_plaintext_snapshot'
  ),
  'ALTER TABLE staff ADD COLUMN password_plaintext_snapshot VARCHAR(255) DEFAULT NULL COMMENT ''密码明文快照（仅限授权查看）'' AFTER password_hash',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS (
    SELECT 1
    FROM information_schema.tables
    WHERE table_schema = @schema
      AND table_name = 'staff'
  )
  AND NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = @schema
      AND table_name = 'staff'
      AND column_name = 'password_snapshot_updated_at'
  ),
  'ALTER TABLE staff ADD COLUMN password_snapshot_updated_at DATETIME DEFAULT NULL COMMENT ''密码快照更新时间'' AFTER password_plaintext_snapshot',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
