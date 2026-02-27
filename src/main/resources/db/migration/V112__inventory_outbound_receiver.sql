-- 出库溯源增强：领取人

SET @has_receiver_name := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'inventory_log'
    AND COLUMN_NAME = 'receiver_name'
);
SET @sql_receiver_name := IF(
  @has_receiver_name = 0,
  'ALTER TABLE inventory_log ADD COLUMN receiver_name VARCHAR(64) DEFAULT NULL COMMENT ''领取人'' AFTER biz_type',
  'SELECT 1'
);
PREPARE stmt FROM @sql_receiver_name;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_receiver_name := (
  SELECT COUNT(1)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'inventory_log'
    AND INDEX_NAME = 'idx_inventory_log_receiver_name'
);
SET @sql_idx_receiver_name := IF(
  @idx_receiver_name = 0,
  'CREATE INDEX idx_inventory_log_receiver_name ON inventory_log (receiver_name)',
  'SELECT 1'
);
PREPARE stmt FROM @sql_idx_receiver_name;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
