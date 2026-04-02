SET @uk_bill_monthly_exists := (
  SELECT COUNT(1)
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'bill_monthly'
    AND INDEX_NAME = 'uk_bill_monthly_org_elder_month_del'
);
SET @uk_bill_monthly_sql := IF(
  @uk_bill_monthly_exists = 0,
  'ALTER TABLE bill_monthly ADD UNIQUE KEY uk_bill_monthly_org_elder_month_del (org_id, elder_id, bill_month, is_deleted)',
  'SELECT 1'
);
PREPARE stmt FROM @uk_bill_monthly_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @uk_payment_record_exists := (
  SELECT COUNT(1)
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'payment_record'
    AND INDEX_NAME = 'uk_payment_record_org_external_del'
);
SET @uk_payment_record_sql := IF(
  @uk_payment_record_exists = 0,
  'ALTER TABLE payment_record ADD UNIQUE KEY uk_payment_record_org_external_del (org_id, external_txn_id, is_deleted)',
  'SELECT 1'
);
PREPARE stmt FROM @uk_payment_record_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @uk_reconciliation_daily_exists := (
  SELECT COUNT(1)
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'reconciliation_daily'
    AND INDEX_NAME = 'uk_reconciliation_daily_org_date_del'
);
SET @uk_reconciliation_daily_sql := IF(
  @uk_reconciliation_daily_exists = 0,
  'ALTER TABLE reconciliation_daily ADD UNIQUE KEY uk_reconciliation_daily_org_date_del (org_id, reconcile_date, is_deleted)',
  'SELECT 1'
);
PREPARE stmt FROM @uk_reconciliation_daily_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @uk_elder_account_legacy_exists := (
  SELECT COUNT(1)
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder_account'
    AND INDEX_NAME = 'uk_elder_account_elder'
);
SET @drop_legacy_elder_account_sql := IF(
  @uk_elder_account_legacy_exists > 0,
  'ALTER TABLE elder_account DROP INDEX uk_elder_account_elder',
  'SELECT 1'
);
PREPARE stmt FROM @drop_legacy_elder_account_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @uk_elder_account_scoped_exists := (
  SELECT COUNT(1)
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder_account'
    AND INDEX_NAME = 'uk_elder_account_org_elder_del'
);
SET @uk_elder_account_scoped_sql := IF(
  @uk_elder_account_scoped_exists = 0,
  'ALTER TABLE elder_account ADD UNIQUE KEY uk_elder_account_org_elder_del (org_id, elder_id, is_deleted)',
  'SELECT 1'
);
PREPARE stmt FROM @uk_elder_account_scoped_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
