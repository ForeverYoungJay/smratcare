-- AI 健康风险预测：评分结果（长者×风险类型，保留历史用于趋势）
CREATE TABLE IF NOT EXISTS ai_risk_score (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NULL,
  org_id BIGINT NULL,
  elder_id BIGINT NOT NULL,
  elder_name VARCHAR(64) NULL,
  risk_type VARCHAR(32) NOT NULL,
  model_id BIGINT NULL,
  score DECIMAL(8,2) NOT NULL DEFAULT 0,
  risk_level VARCHAR(16) NOT NULL DEFAULT 'LOW',
  factor_json TEXT NULL,
  assess_time DATETIME NOT NULL,
  assess_date DATE NOT NULL,
  source VARCHAR(16) NOT NULL DEFAULT 'AUTO',
  alert_id BIGINT NULL,
  created_by BIGINT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_ai_risk_score_day (org_id, elder_id, risk_type, assess_date, is_deleted),
  KEY idx_ai_risk_score_org (org_id, risk_type, risk_level, is_deleted),
  KEY idx_ai_risk_score_elder (org_id, elder_id, risk_type, assess_date)
);
