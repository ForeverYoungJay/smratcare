-- 药品批次库存（支持批号/效期，FEFO 先过期先出）
CREATE TABLE IF NOT EXISTS drug_batch (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NULL,
  org_id BIGINT NOT NULL,
  drug_id BIGINT NOT NULL,
  batch_no VARCHAR(64) NOT NULL,
  expire_date DATE NULL,
  production_date DATE NULL,
  quantity INT NOT NULL DEFAULT 0,
  purchase_price BIGINT NOT NULL DEFAULT 0,
  supplier VARCHAR(180) NULL,
  inbound_at DATETIME NULL,
  status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
  remark VARCHAR(500) NULL,
  created_by BIGINT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_drug_batch (org_id, drug_id, batch_no, is_deleted),
  KEY idx_drug_batch_fefo (org_id, drug_id, expire_date),
  KEY idx_drug_batch_expire (org_id, expire_date, status)
);
