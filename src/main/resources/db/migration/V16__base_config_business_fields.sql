-- Base config business fields (idempotent for existing databases)

SET @crm_lead_customer_tag_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_lead'
    AND COLUMN_NAME = 'customer_tag'
);
SET @ddl = IF(
  @crm_lead_customer_tag_exists = 0,
  'ALTER TABLE crm_lead ADD COLUMN customer_tag VARCHAR(64) DEFAULT NULL COMMENT ''客户标签'' AFTER source',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @trial_stay_trial_package_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder_trial_stay'
    AND COLUMN_NAME = 'trial_package'
);
SET @ddl = IF(
  @trial_stay_trial_package_exists = 0,
  'ALTER TABLE elder_trial_stay ADD COLUMN trial_package VARCHAR(64) DEFAULT NULL COMMENT ''试住套餐'' AFTER channel',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
