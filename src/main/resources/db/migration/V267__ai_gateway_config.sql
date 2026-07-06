-- AI 网关适配层配置：当前内置规则引擎，为后续接入大模型预留扩展点
CREATE TABLE IF NOT EXISTS ai_gateway_config (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NULL,
  org_id BIGINT NULL,
  provider VARCHAR(32) NOT NULL DEFAULT 'RULE_ENGINE',
  endpoint VARCHAR(300) NULL,
  api_key VARCHAR(300) NULL,
  model_name VARCHAR(100) NULL,
  timeout_ms INT NOT NULL DEFAULT 30000,
  enabled TINYINT NOT NULL DEFAULT 1,
  remark VARCHAR(500) NULL,
  created_by BIGINT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_ai_gateway_org (org_id, is_deleted)
);

-- 全局默认：规则引擎实现
INSERT INTO ai_gateway_config (id, org_id, provider, model_name, enabled, remark)
SELECT 26701, NULL, 'RULE_ENGINE', 'builtin-rule-v1', 1, '内置规则引擎（默认），可切换为 LLM 网关'
WHERE NOT EXISTS (SELECT 1 FROM ai_gateway_config WHERE id = 26701);
