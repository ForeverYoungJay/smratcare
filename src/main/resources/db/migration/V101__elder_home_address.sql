-- M2 长者档案补充：家庭地址（兼容不支持 IF NOT EXISTS 的 MySQL 版本）

SET @home_addr_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder'
    AND COLUMN_NAME = 'home_address'
);

SET @ddl = IF(
  @home_addr_exists = 0,
  'ALTER TABLE elder ADD COLUMN home_address VARCHAR(255) DEFAULT NULL COMMENT ''家庭地址'' AFTER phone',
  'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
