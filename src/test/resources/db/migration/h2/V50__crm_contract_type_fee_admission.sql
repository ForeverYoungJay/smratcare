ALTER TABLE crm_contract ADD COLUMN IF NOT EXISTS contract_type VARCHAR(32);
ALTER TABLE crm_contract ADD COLUMN IF NOT EXISTS reference_monthly_fee DECIMAL(12,2);
ALTER TABLE crm_contract ADD COLUMN IF NOT EXISTS expected_admission_date DATE;
