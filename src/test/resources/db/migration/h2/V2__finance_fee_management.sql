CREATE TABLE finance_admission_fee_audit (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  org_id BIGINT NOT NULL,
  elder_id BIGINT NOT NULL,
  elder_name VARCHAR(64),
  admission_id BIGINT,
  total_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  deposit_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  audit_remark VARCHAR(255),
  reviewed_by BIGINT,
  reviewed_time DATETIME,
  created_by BIGINT,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE finance_discharge_fee_audit (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  org_id BIGINT NOT NULL,
  elder_id BIGINT NOT NULL,
  elder_name VARCHAR(64),
  discharge_apply_id BIGINT,
  payable_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  audit_remark VARCHAR(255),
  reviewed_by BIGINT,
  reviewed_time DATETIME,
  created_by BIGINT,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE finance_discharge_settlement (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  org_id BIGINT NOT NULL,
  elder_id BIGINT NOT NULL,
  elder_name VARCHAR(64),
  discharge_apply_id BIGINT,
  payable_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  from_deposit_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  refund_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  supplement_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING_CONFIRM',
  remark VARCHAR(255),
  settled_by BIGINT,
  settled_time DATETIME,
  created_by BIGINT,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE finance_consumption_record (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  org_id BIGINT NOT NULL,
  elder_id BIGINT NOT NULL,
  elder_name VARCHAR(64),
  consume_date DATE NOT NULL,
  amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  category VARCHAR(64),
  source_type VARCHAR(32),
  source_id BIGINT,
  remark VARCHAR(255),
  created_by BIGINT,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE finance_monthly_allocation (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  org_id BIGINT NOT NULL,
  allocation_month CHAR(7) NOT NULL,
  allocation_name VARCHAR(64) NOT NULL,
  total_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  target_count INT NOT NULL DEFAULT 0,
  status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
  remark VARCHAR(255),
  reviewed_by BIGINT,
  reviewed_time DATETIME,
  created_by BIGINT,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);
