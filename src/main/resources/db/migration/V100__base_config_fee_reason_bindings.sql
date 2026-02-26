-- Finance discharge fee reason bindings (idempotent)

SET @audit_fee_item_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'finance_discharge_fee_audit'
    AND COLUMN_NAME = 'fee_item'
);
SET @ddl = IF(
  @audit_fee_item_exists = 0,
  'ALTER TABLE finance_discharge_fee_audit ADD COLUMN fee_item VARCHAR(64) DEFAULT NULL COMMENT ''费用项'' AFTER payable_amount',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @audit_discharge_fee_config_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'finance_discharge_fee_audit'
    AND COLUMN_NAME = 'discharge_fee_config'
);
SET @ddl = IF(
  @audit_discharge_fee_config_exists = 0,
  'ALTER TABLE finance_discharge_fee_audit ADD COLUMN discharge_fee_config VARCHAR(64) DEFAULT NULL COMMENT ''退住费用设置'' AFTER fee_item',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @settlement_fee_item_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'finance_discharge_settlement'
    AND COLUMN_NAME = 'fee_item'
);
SET @ddl = IF(
  @settlement_fee_item_exists = 0,
  'ALTER TABLE finance_discharge_settlement ADD COLUMN fee_item VARCHAR(64) DEFAULT NULL COMMENT ''费用项'' AFTER payable_amount',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @settlement_discharge_fee_config_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'finance_discharge_settlement'
    AND COLUMN_NAME = 'discharge_fee_config'
);
SET @ddl = IF(
  @settlement_discharge_fee_config_exists = 0,
  'ALTER TABLE finance_discharge_settlement ADD COLUMN discharge_fee_config VARCHAR(64) DEFAULT NULL COMMENT ''退住费用设置'' AFTER fee_item',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
