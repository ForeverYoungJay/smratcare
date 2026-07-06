-- 医保结算清单（由长护险结算单生成；金额单位：分）
-- 状态机：DRAFT 草稿 → UPLOADED 已上传 → RECEIPTED 已回执 → RECONCILED 已对账；REJECTED 驳回（可重传）
CREATE TABLE IF NOT EXISTS medins_settlement_sheet (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NULL,
  org_id BIGINT NOT NULL,
  ltci_settlement_id BIGINT NOT NULL,
  elder_id BIGINT NOT NULL,
  sheet_no VARCHAR(64) NULL,
  settle_month VARCHAR(6) NOT NULL,
  total_fee BIGINT NOT NULL DEFAULT 0,
  fund_pay BIGINT NOT NULL DEFAULT 0,
  self_pay BIGINT NOT NULL DEFAULT 0,
  sheet_status VARCHAR(24) NOT NULL DEFAULT 'DRAFT',
  channel VARCHAR(32) NULL,
  receipt_code VARCHAR(80) NULL,
  reject_reason VARCHAR(500) NULL,
  uploaded_at DATETIME NULL,
  receipted_at DATETIME NULL,
  reconciled_at DATETIME NULL,
  remark VARCHAR(500) NULL,
  created_by BIGINT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_medins_sheet_settle (org_id, ltci_settlement_id, is_deleted),
  KEY idx_medins_sheet_month (org_id, settle_month, sheet_status),
  KEY idx_medins_sheet_elder (org_id, elder_id)
);
