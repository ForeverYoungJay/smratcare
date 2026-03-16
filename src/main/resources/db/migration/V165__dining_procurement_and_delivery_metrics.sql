SET @dish_current_dining_count_exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'dining_dish'
    AND COLUMN_NAME = 'current_dining_count'
);
SET @dish_purchase_qty_exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'dining_dish'
    AND COLUMN_NAME = 'purchase_qty'
);
SET @dish_purchase_unit_exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'dining_dish'
    AND COLUMN_NAME = 'purchase_unit'
);
SET @order_expected_delivery_time_exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'dining_meal_order'
    AND COLUMN_NAME = 'expected_delivery_time'
);

SET @sql = IF(
  @dish_current_dining_count_exists = 0,
  'ALTER TABLE dining_dish ADD COLUMN current_dining_count INT NOT NULL DEFAULT 0 COMMENT ''现行用餐人数'' AFTER unit_price',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
  @dish_purchase_qty_exists = 0,
  'ALTER TABLE dining_dish ADD COLUMN purchase_qty DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT ''单次采购用量'' AFTER current_dining_count',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
  @dish_purchase_unit_exists = 0,
  'ALTER TABLE dining_dish ADD COLUMN purchase_unit VARCHAR(16) DEFAULT ''斤'' COMMENT ''采购用量单位'' AFTER purchase_qty',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
  @order_expected_delivery_time_exists = 0,
  'ALTER TABLE dining_meal_order ADD COLUMN expected_delivery_time DATETIME DEFAULT NULL COMMENT ''期望送达时间'' AFTER order_date',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
