-- 长护险参保登记与待遇资格
CREATE TABLE IF NOT EXISTS ltci_insured (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NULL,
  org_id BIGINT NOT NULL,
  elder_id BIGINT NOT NULL,
  insured_no VARCHAR(64) NOT NULL,
  id_card VARCHAR(64) NULL,
  city_code VARCHAR(32) NULL,
  insure_status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
  start_date DATE NULL,
  end_date DATE NULL,
  remark VARCHAR(500) NULL,
  created_by BIGINT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_ltci_insured_no (org_id, insured_no, is_deleted),
  KEY idx_ltci_insured_elder (org_id, elder_id, is_deleted)
);

CREATE TABLE IF NOT EXISTS ltci_benefit (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NULL,
  org_id BIGINT NOT NULL,
  insured_id BIGINT NOT NULL,
  elder_id BIGINT NOT NULL,
  assessment_id BIGINT NULL,
  disability_level TINYINT NULL,
  benefit_type VARCHAR(24) NOT NULL DEFAULT 'INSTITUTION',
  daily_quota BIGINT NOT NULL DEFAULT 0,
  pay_ratio DECIMAL(5,4) NOT NULL DEFAULT 0.0000,
  benefit_status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
  valid_start DATE NULL,
  valid_end DATE NULL,
  remark VARCHAR(500) NULL,
  created_by BIGINT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_ltci_benefit_elder (org_id, elder_id, is_deleted),
  KEY idx_ltci_benefit_insured (org_id, insured_id),
  KEY idx_ltci_benefit_valid (org_id, elder_id, valid_start, valid_end)
);
