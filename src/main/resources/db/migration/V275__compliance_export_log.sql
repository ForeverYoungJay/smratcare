-- 合规与数据安全：导出二次确认与留痕（谁/何时/导出什么模块什么范围/多少行）
CREATE TABLE IF NOT EXISTS compliance_export_log (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NULL,
  org_id BIGINT NOT NULL,
  actor_id BIGINT NULL,
  actor_name VARCHAR(120) NULL,
  actor_role VARCHAR(120) NULL,
  module VARCHAR(64) NOT NULL,                          -- ELDER_LIST / FAMILY_LIST / BILL ...
  scope VARCHAR(500) NULL,                              -- 导出范围描述（筛选条件等）
  purpose VARCHAR(255) NULL,                            -- 导出用途（二次确认时填写）
  row_count INT NULL,                                   -- 实际导出行数（导出完成后回填）
  export_ticket VARCHAR(64) NOT NULL,                   -- 一次性导出凭证
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING',        -- PENDING / USED / EXPIRED
  expires_at DATETIME NULL,
  used_at DATETIME NULL,
  ip VARCHAR(64) NULL,
  user_agent VARCHAR(255) NULL,
  request_id VARCHAR(64) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_cel_ticket (export_ticket),
  KEY idx_cel_org_time (org_id, create_time),
  KEY idx_cel_actor (org_id, actor_id, create_time)
);
