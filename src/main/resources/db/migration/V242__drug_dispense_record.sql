-- 发药/退药记录（按批次扣减，可关联长者与医嘱）
CREATE TABLE IF NOT EXISTS drug_dispense_record (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NULL,
  org_id BIGINT NOT NULL,
  drug_id BIGINT NOT NULL,
  batch_id BIGINT NULL,
  batch_no VARCHAR(64) NULL,
  elder_id BIGINT NULL,
  order_id BIGINT NULL,
  dispense_type VARCHAR(16) NOT NULL DEFAULT 'DISPENSE',
  quantity INT NOT NULL DEFAULT 0,
  operator_id BIGINT NULL,
  dispense_time DATETIME NULL,
  remark VARCHAR(500) NULL,
  created_by BIGINT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_dispense_drug (org_id, drug_id, dispense_time),
  KEY idx_dispense_elder (org_id, elder_id),
  KEY idx_dispense_order (org_id, order_id)
);
