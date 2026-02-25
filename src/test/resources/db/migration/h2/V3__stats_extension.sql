CREATE TABLE IF NOT EXISTS elder_admission (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  org_id BIGINT NOT NULL,
  elder_id BIGINT NOT NULL,
  admission_date DATE NOT NULL,
  contract_no VARCHAR(64) DEFAULT NULL,
  deposit_amount DECIMAL(12,2) DEFAULT NULL,
  remark VARCHAR(255) DEFAULT NULL,
  created_by BIGINT DEFAULT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS elder_discharge (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  org_id BIGINT NOT NULL,
  elder_id BIGINT NOT NULL,
  discharge_date DATE NOT NULL,
  reason VARCHAR(255) DEFAULT NULL,
  settle_amount DECIMAL(12,2) DEFAULT NULL,
  remark VARCHAR(255) DEFAULT NULL,
  created_by BIGINT DEFAULT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

INSERT INTO org (id, org_code, org_name, org_type, status)
VALUES (2, 'ORG002', '第二测试机构', '养老院', 1);

INSERT INTO elder (id, org_id, elder_code, elder_qr_code, full_name, status)
VALUES (9001, 2, 'E9001', 'EQR9001', 'Org2 Elder', 1);

INSERT INTO elder_admission (id, org_id, elder_id, admission_date, contract_no, deposit_amount, remark, created_by)
VALUES (9101, 2, 9001, DATE '2026-01-10', 'C-ORG2-1', 1000.00, 'org2 admission', 500);

INSERT INTO elder_discharge (id, org_id, elder_id, discharge_date, reason, settle_amount, remark, created_by)
VALUES (9201, 2, 9001, DATE '2026-01-20', 'test discharge', 300.00, 'org2 discharge', 500);

INSERT INTO elder_admission (id, org_id, elder_id, admission_date, contract_no, deposit_amount, remark, created_by)
VALUES (9102, 1, 200, DATE '2026-01-05', 'C-ORG1-1', 800.00, 'org1 admission', 500);
