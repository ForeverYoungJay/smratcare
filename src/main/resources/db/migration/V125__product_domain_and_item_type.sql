SET @col_exists_business_domain := (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'product'
    AND column_name = 'business_domain'
);
SET @sql_add_business_domain := IF(
  @col_exists_business_domain = 0,
  'ALTER TABLE product ADD COLUMN business_domain VARCHAR(16) NOT NULL DEFAULT ''BOTH'' COMMENT ''业务域 INTERNAL/MALL/BOTH''',
  'SELECT 1'
);
PREPARE stmt_add_business_domain FROM @sql_add_business_domain;
EXECUTE stmt_add_business_domain;
DEALLOCATE PREPARE stmt_add_business_domain;

SET @col_exists_item_type := (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'product'
    AND column_name = 'item_type'
);
SET @sql_add_item_type := IF(
  @col_exists_item_type = 0,
  'ALTER TABLE product ADD COLUMN item_type VARCHAR(16) NOT NULL DEFAULT ''CONSUMABLE'' COMMENT ''物资类型 ASSET/CONSUMABLE/FOOD/SERVICE''',
  'SELECT 1'
);
PREPARE stmt_add_item_type FROM @sql_add_item_type;
EXECUTE stmt_add_item_type;
DEALLOCATE PREPARE stmt_add_item_type;

SET @col_exists_mall_enabled := (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'product'
    AND column_name = 'mall_enabled'
);
SET @sql_add_mall_enabled := IF(
  @col_exists_mall_enabled = 0,
  'ALTER TABLE product ADD COLUMN mall_enabled TINYINT NOT NULL DEFAULT 1 COMMENT ''是否商城可售 1是0否''',
  'SELECT 1'
);
PREPARE stmt_add_mall_enabled FROM @sql_add_mall_enabled;
EXECUTE stmt_add_mall_enabled;
DEALLOCATE PREPARE stmt_add_mall_enabled;

UPDATE product
SET business_domain = 'BOTH'
WHERE business_domain IS NULL OR business_domain = '';

UPDATE product
SET item_type = 'CONSUMABLE'
WHERE item_type IS NULL OR item_type = '';

UPDATE product
SET mall_enabled = 1
WHERE mall_enabled IS NULL;

SET @idx_exists := (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'product'
    AND index_name = 'idx_product_domain_type'
);
SET @create_idx_sql := IF(
  @idx_exists = 0,
  'CREATE INDEX idx_product_domain_type ON product (org_id, business_domain, item_type, mall_enabled, status)',
  'SELECT 1'
);
PREPARE stmt_create_idx FROM @create_idx_sql;
EXECUTE stmt_create_idx;
DEALLOCATE PREPARE stmt_create_idx;
