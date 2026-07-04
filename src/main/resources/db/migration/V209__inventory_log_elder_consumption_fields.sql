-- 库存出库流水补齐长者归集字段

SET @has_inventory_log_elder_id := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'inventory_log'
    AND COLUMN_NAME = 'elder_id'
);
SET @sql_inventory_log_elder_id := IF(
  @has_inventory_log_elder_id = 0,
  'ALTER TABLE inventory_log ADD COLUMN elder_id BIGINT DEFAULT NULL COMMENT ''长者ID'' AFTER receiver_name',
  'SELECT 1'
);
PREPARE stmt FROM @sql_inventory_log_elder_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_inventory_log_elder_name := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'inventory_log'
    AND COLUMN_NAME = 'elder_name'
);
SET @sql_inventory_log_elder_name := IF(
  @has_inventory_log_elder_name = 0,
  'ALTER TABLE inventory_log ADD COLUMN elder_name VARCHAR(64) DEFAULT NULL COMMENT ''长者姓名'' AFTER elder_id',
  'SELECT 1'
);
PREPARE stmt FROM @sql_inventory_log_elder_name;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_inventory_log_elder_id := (
  SELECT COUNT(1)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'inventory_log'
    AND INDEX_NAME = 'idx_inventory_log_elder_id'
);
SET @sql_idx_inventory_log_elder_id := IF(
  @idx_inventory_log_elder_id = 0,
  'CREATE INDEX idx_inventory_log_elder_id ON inventory_log (elder_id)',
  'SELECT 1'
);
PREPARE stmt FROM @sql_idx_inventory_log_elder_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
