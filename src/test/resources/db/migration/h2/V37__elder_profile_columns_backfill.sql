-- H2 测试库的 elder 表停留在 V1__init 的字段集，缺少主库 V111 及之后补的档案字段，
-- 导致 ElderMapper.selectById 直接 BadSqlGrammar（FinancePaymentTest 等一批用例长期报错）。
-- 这里按 ElderProfile 实体补齐缺失列（主库定义见 V111/V2 等）。

ALTER TABLE elder ADD COLUMN IF NOT EXISTS medical_insurance_copy_url VARCHAR(512) DEFAULT NULL;
ALTER TABLE elder ADD COLUMN IF NOT EXISTS household_copy_url VARCHAR(512) DEFAULT NULL;
ALTER TABLE elder ADD COLUMN IF NOT EXISTS medical_record_file_url VARCHAR(512) DEFAULT NULL;
ALTER TABLE elder ADD COLUMN IF NOT EXISTS lifecycle_status VARCHAR(32) DEFAULT NULL;
ALTER TABLE elder ADD COLUMN IF NOT EXISTS departure_type VARCHAR(32) DEFAULT NULL;
ALTER TABLE elder ADD COLUMN IF NOT EXISTS lifecycle_updated_at DATETIME DEFAULT NULL;
ALTER TABLE elder ADD COLUMN IF NOT EXISTS last_lifecycle_event_id BIGINT DEFAULT NULL;
ALTER TABLE elder ADD COLUMN IF NOT EXISTS risk_precommit VARCHAR(512) DEFAULT NULL;
ALTER TABLE elder ADD COLUMN IF NOT EXISTS source_type VARCHAR(32) DEFAULT NULL;
ALTER TABLE elder ADD COLUMN IF NOT EXISTS historical_contract_file_url VARCHAR(512) DEFAULT NULL;
