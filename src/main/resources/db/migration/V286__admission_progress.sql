-- 入住办理中心：保存跨部门办理的暂存草稿、当前步骤与"退回补充"状态（其余步骤状态由评估/合同/费用/床位等既有数据推导）。

CREATE TABLE IF NOT EXISTS elder_admission_progress (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NULL,
  org_id BIGINT NOT NULL,
  elder_id BIGINT NOT NULL,
  contract_no VARCHAR(64) NULL,
  current_step INT NOT NULL DEFAULT 1 COMMENT '1 入住登记 … 6 入住确认',
  status VARCHAR(16) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT 办理中 / RETURNED 退回补充',
  return_reason VARCHAR(200) NULL,
  draft_json TEXT NULL COMMENT '入住确认表单暂存（日期/押金/床位/备注）',
  updated_by BIGINT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_elder_admission_progress (org_id, elder_id, is_deleted)
) COMMENT='入住办理进度与暂存草稿';
