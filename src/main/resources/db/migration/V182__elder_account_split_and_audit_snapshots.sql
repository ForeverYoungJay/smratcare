SET @elder_account_deposit_exists := (
  SELECT COUNT(1)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder_account'
    AND COLUMN_NAME = 'deposit_balance'
);
SET @elder_account_deposit_sql := IF(
  @elder_account_deposit_exists = 0,
  'ALTER TABLE elder_account ADD COLUMN deposit_balance DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT ''押金余额'' AFTER balance',
  'SELECT 1'
);
PREPARE stmt FROM @elder_account_deposit_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @elder_account_prepaid_exists := (
  SELECT COUNT(1)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder_account'
    AND COLUMN_NAME = 'prepaid_balance'
);
SET @elder_account_prepaid_sql := IF(
  @elder_account_prepaid_exists = 0,
  'ALTER TABLE elder_account ADD COLUMN prepaid_balance DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT ''预收余额'' AFTER deposit_balance',
  'SELECT 1'
);
PREPARE stmt FROM @elder_account_prepaid_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE elder_account
SET prepaid_balance = balance
WHERE COALESCE(balance, 0) <> 0
  AND COALESCE(deposit_balance, 0) = 0
  AND COALESCE(prepaid_balance, 0) = 0;

SET @elder_account_log_fund_type_exists := (
  SELECT COUNT(1)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder_account_log'
    AND COLUMN_NAME = 'fund_type'
);
SET @elder_account_log_fund_type_sql := IF(
  @elder_account_log_fund_type_exists = 0,
  'ALTER TABLE elder_account_log ADD COLUMN fund_type VARCHAR(16) DEFAULT NULL COMMENT ''资金类型 PREPAID/DEPOSIT/AUTO'' AFTER direction',
  'SELECT 1'
);
PREPARE stmt FROM @elder_account_log_fund_type_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @elder_account_log_deposit_after_exists := (
  SELECT COUNT(1)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder_account_log'
    AND COLUMN_NAME = 'deposit_balance_after'
);
SET @elder_account_log_deposit_after_sql := IF(
  @elder_account_log_deposit_after_exists = 0,
  'ALTER TABLE elder_account_log ADD COLUMN deposit_balance_after DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT ''变动后押金余额'' AFTER fund_type',
  'SELECT 1'
);
PREPARE stmt FROM @elder_account_log_deposit_after_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @elder_account_log_prepaid_after_exists := (
  SELECT COUNT(1)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'elder_account_log'
    AND COLUMN_NAME = 'prepaid_balance_after'
);
SET @elder_account_log_prepaid_after_sql := IF(
  @elder_account_log_prepaid_after_exists = 0,
  'ALTER TABLE elder_account_log ADD COLUMN prepaid_balance_after DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT ''变动后预收余额'' AFTER deposit_balance_after',
  'SELECT 1'
);
PREPARE stmt FROM @elder_account_log_prepaid_after_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE elder_account_log
SET fund_type = COALESCE(fund_type, 'AUTO'),
    deposit_balance_after = COALESCE(deposit_balance_after, 0),
    prepaid_balance_after = CASE
      WHEN COALESCE(prepaid_balance_after, 0) = 0 AND COALESCE(balance_after, 0) <> 0 THEN balance_after
      ELSE COALESCE(prepaid_balance_after, 0)
    END;

SET @audit_before_exists := (
  SELECT COUNT(1)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'audit_log'
    AND COLUMN_NAME = 'before_snapshot'
);
SET @audit_before_sql := IF(
  @audit_before_exists = 0,
  'ALTER TABLE audit_log ADD COLUMN before_snapshot TEXT DEFAULT NULL COMMENT ''变更前快照'' AFTER detail',
  'SELECT 1'
);
PREPARE stmt FROM @audit_before_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @audit_after_exists := (
  SELECT COUNT(1)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'audit_log'
    AND COLUMN_NAME = 'after_snapshot'
);
SET @audit_after_sql := IF(
  @audit_after_exists = 0,
  'ALTER TABLE audit_log ADD COLUMN after_snapshot TEXT DEFAULT NULL COMMENT ''变更后快照'' AFTER before_snapshot',
  'SELECT 1'
);
PREPARE stmt FROM @audit_after_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @audit_context_exists := (
  SELECT COUNT(1)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'audit_log'
    AND COLUMN_NAME = 'context_json'
);
SET @audit_context_sql := IF(
  @audit_context_exists = 0,
  'ALTER TABLE audit_log ADD COLUMN context_json TEXT DEFAULT NULL COMMENT ''上下文信息'' AFTER after_snapshot',
  'SELECT 1'
);
PREPARE stmt FROM @audit_context_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
