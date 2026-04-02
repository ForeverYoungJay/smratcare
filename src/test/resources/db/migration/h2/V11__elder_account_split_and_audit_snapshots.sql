ALTER TABLE elder_account ADD COLUMN IF NOT EXISTS deposit_balance DECIMAL(12,2) NOT NULL DEFAULT 0;
ALTER TABLE elder_account ADD COLUMN IF NOT EXISTS prepaid_balance DECIMAL(12,2) NOT NULL DEFAULT 0;

UPDATE elder_account
SET prepaid_balance = balance
WHERE COALESCE(balance, 0) <> 0
  AND COALESCE(deposit_balance, 0) = 0
  AND COALESCE(prepaid_balance, 0) = 0;

ALTER TABLE elder_account_log ADD COLUMN IF NOT EXISTS fund_type VARCHAR(16);
ALTER TABLE elder_account_log ADD COLUMN IF NOT EXISTS deposit_balance_after DECIMAL(12,2) NOT NULL DEFAULT 0;
ALTER TABLE elder_account_log ADD COLUMN IF NOT EXISTS prepaid_balance_after DECIMAL(12,2) NOT NULL DEFAULT 0;

UPDATE elder_account_log
SET fund_type = COALESCE(fund_type, 'AUTO'),
    deposit_balance_after = COALESCE(deposit_balance_after, 0),
    prepaid_balance_after = CASE
      WHEN COALESCE(prepaid_balance_after, 0) = 0 AND COALESCE(balance_after, 0) <> 0 THEN balance_after
      ELSE COALESCE(prepaid_balance_after, 0)
    END;

ALTER TABLE audit_log ADD COLUMN IF NOT EXISTS before_snapshot CLOB;
ALTER TABLE audit_log ADD COLUMN IF NOT EXISTS after_snapshot CLOB;
ALTER TABLE audit_log ADD COLUMN IF NOT EXISTS context_json CLOB;
