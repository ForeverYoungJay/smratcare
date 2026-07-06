-- 长者医保电子凭证绑定登记与校验记录（凭证令牌仅存授权引用，不落敏感原文）
CREATE TABLE IF NOT EXISTS medins_evoucher (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NULL,
  org_id BIGINT NOT NULL,
  elder_id BIGINT NOT NULL,
  insured_no VARCHAR(64) NULL,
  id_card VARCHAR(32) NULL,
  ecard_token VARCHAR(200) NULL,
  channel VARCHAR(32) NULL,
  bind_status VARCHAR(16) NOT NULL DEFAULT 'BOUND',
  verify_status VARCHAR(16) NULL,
  verify_message VARCHAR(500) NULL,
  last_verify_at DATETIME NULL,
  remark VARCHAR(500) NULL,
  created_by BIGINT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_medins_evoucher_elder (org_id, elder_id, is_deleted),
  KEY idx_medins_evoucher_status (org_id, bind_status, verify_status)
);
