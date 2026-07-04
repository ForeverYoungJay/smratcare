-- 合规与数据安全：敏感数据访问/导出审计日志（个人信息保护 + 等保留痕）
CREATE TABLE IF NOT EXISTS compliance_sensitive_access_log (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NULL,
  org_id BIGINT NOT NULL,
  actor_id BIGINT NULL,
  actor_name VARCHAR(120) NULL,
  actor_role VARCHAR(120) NULL,
  action VARCHAR(32) NOT NULL,            -- VIEW / EXPORT / DECRYPT / PRINT
  data_category VARCHAR(64) NOT NULL,     -- ELDER_HEALTH / ELDER_IDCARD / FAMILY_PRIVACY / BILL ...
  target_type VARCHAR(64) NULL,           -- ELDER / FAMILY / BILL ...
  target_id BIGINT NULL,
  target_name VARCHAR(120) NULL,
  fields VARCHAR(500) NULL,               -- 涉及的敏感字段（逗号分隔）
  purpose VARCHAR(255) NULL,              -- 访问用途/业务场景
  result VARCHAR(16) NOT NULL DEFAULT 'SUCCESS', -- SUCCESS / DENIED
  ip VARCHAR(64) NULL,
  user_agent VARCHAR(255) NULL,
  request_id VARCHAR(64) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_csal_org_time (org_id, create_time),
  KEY idx_csal_actor (org_id, actor_id, create_time),
  KEY idx_csal_target (org_id, target_type, target_id),
  KEY idx_csal_category (org_id, data_category, action)
);
