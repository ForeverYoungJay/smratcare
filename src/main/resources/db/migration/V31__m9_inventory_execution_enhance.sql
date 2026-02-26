-- M9 第二批：库存按仓维度与执行联动增强（幂等）

SET @batch_warehouse_col_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'inventory_batch'
    AND COLUMN_NAME = 'warehouse_id'
);
SET @ddl = IF(
  @batch_warehouse_col_exists = 0,
  'ALTER TABLE inventory_batch ADD COLUMN warehouse_id BIGINT DEFAULT NULL COMMENT ''仓库ID'' AFTER product_id',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_batch_warehouse_exists = (
  SELECT COUNT(1)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'inventory_batch'
    AND INDEX_NAME = 'idx_inventory_batch_warehouse_id'
);
SET @ddl = IF(
  @idx_batch_warehouse_exists = 0,
  'CREATE INDEX idx_inventory_batch_warehouse_id ON inventory_batch (warehouse_id)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @log_warehouse_col_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'inventory_log'
    AND COLUMN_NAME = 'warehouse_id'
);
SET @ddl = IF(
  @log_warehouse_col_exists = 0,
  'ALTER TABLE inventory_log ADD COLUMN warehouse_id BIGINT DEFAULT NULL COMMENT ''仓库ID'' AFTER batch_id',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_log_warehouse_exists = (
  SELECT COUNT(1)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'inventory_log'
    AND INDEX_NAME = 'idx_inventory_log_warehouse_id'
);
SET @ddl = IF(
  @idx_log_warehouse_exists = 0,
  'CREATE INDEX idx_inventory_log_warehouse_id ON inventory_log (warehouse_id)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @uk_has_warehouse = (
  SELECT COUNT(1)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'inventory_batch'
    AND INDEX_NAME = 'uk_inventory_batch'
    AND COLUMN_NAME = 'warehouse_id'
);
SET @uk_exists = (
  SELECT COUNT(1)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'inventory_batch'
    AND INDEX_NAME = 'uk_inventory_batch'
);
SET @ddl = IF(
  @uk_has_warehouse > 0,
  'SELECT 1',
  IF(
    @uk_exists > 0,
    'ALTER TABLE inventory_batch DROP INDEX uk_inventory_batch, ADD UNIQUE KEY uk_inventory_batch (org_id, product_id, batch_no, warehouse_id)',
    'ALTER TABLE inventory_batch ADD UNIQUE KEY uk_inventory_batch (org_id, product_id, batch_no, warehouse_id)'
  )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
