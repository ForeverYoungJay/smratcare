-- AI 健康风险预测：风险评分模型配置（规则JSON：因子+权重+阈值分级）
CREATE TABLE IF NOT EXISTS ai_risk_model (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NULL,
  org_id BIGINT NULL,
  risk_type VARCHAR(32) NOT NULL,
  model_name VARCHAR(150) NOT NULL,
  rule_json TEXT NOT NULL,
  enabled TINYINT NOT NULL DEFAULT 1,
  remark VARCHAR(500) NULL,
  created_by BIGINT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_ai_risk_model_type (org_id, risk_type, is_deleted),
  KEY idx_ai_risk_model_org (org_id, enabled)
);

-- 内置默认规则（org_id 为 NULL 表示全局默认）
INSERT INTO ai_risk_model (id, org_id, risk_type, model_name, rule_json, enabled, remark)
SELECT 26501, NULL, 'FALL', '跌倒风险默认模型',
'{"factors":[{"code":"AGE","name":"年龄","weight":1.0,"bands":[{"gte":80,"score":25},{"gte":70,"score":15},{"gte":65,"score":8}]},{"code":"FALL_HISTORY_90D","name":"近90天跌倒次数","weight":1.0,"bands":[{"gte":2,"score":40},{"gte":1,"score":25}]},{"code":"SEDATIVE_MED_30D","name":"近30天镇静类用药","weight":1.0,"bands":[{"gte":1,"score":15}]},{"code":"DISABILITY_LEVEL","name":"失能等级","weight":1.0,"bands":[{"gte":3,"score":20},{"gte":2,"score":12},{"gte":1,"score":6}]}],"levels":[{"level":"HIGH","gte":60},{"level":"MEDIUM","gte":30},{"level":"LOW","gte":0}]}',
1, '因子：年龄/近90天跌倒史(不良事件)/镇静类用药(用药登记)/失能等级(评估)'
WHERE NOT EXISTS (SELECT 1 FROM ai_risk_model WHERE id = 26501);

INSERT INTO ai_risk_model (id, org_id, risk_type, model_name, rule_json, enabled, remark)
SELECT 26502, NULL, 'PRESSURE_ULCER', '压疮风险默认模型',
'{"factors":[{"code":"BEDRIDDEN","name":"长期卧床","weight":1.0,"bands":[{"gte":1,"score":30}]},{"code":"INCONTINENCE","name":"失禁","weight":1.0,"bands":[{"gte":1,"score":20}]},{"code":"BMI","name":"BMI偏低","weight":1.0,"bands":[{"lte":18.5,"score":15}]},{"code":"ALBUMIN","name":"白蛋白偏低(g/L)","weight":1.0,"bands":[{"lte":35,"score":15}]},{"code":"DISABILITY_LEVEL","name":"失能等级","weight":1.0,"bands":[{"gte":3,"score":15},{"gte":2,"score":8}]}],"levels":[{"level":"HIGH","gte":55},{"level":"MEDIUM","gte":30},{"level":"LOW","gte":0}]}',
1, '因子：卧床/失禁/BMI/白蛋白(如有健康数据)/失能等级'
WHERE NOT EXISTS (SELECT 1 FROM ai_risk_model WHERE id = 26502);

INSERT INTO ai_risk_model (id, org_id, risk_type, model_name, rule_json, enabled, remark)
SELECT 26503, NULL, 'READMISSION', '再入院风险默认模型',
'{"factors":[{"code":"HOSPITALIZATION_180D","name":"近半年住院/就医次数","weight":1.0,"bands":[{"gte":3,"score":45},{"gte":2,"score":30},{"gte":1,"score":15}]},{"code":"CHRONIC_DISEASE_COUNT","name":"慢病数量","weight":1.0,"bands":[{"gte":3,"score":25},{"gte":1,"score":10}]},{"code":"AGE","name":"年龄","weight":1.0,"bands":[{"gte":80,"score":15},{"gte":70,"score":8}]}],"levels":[{"level":"HIGH","gte":55},{"level":"MEDIUM","gte":25},{"level":"LOW","gte":0}]}',
1, '因子：近半年就医外出记录次数/健康档案慢病数量/年龄'
WHERE NOT EXISTS (SELECT 1 FROM ai_risk_model WHERE id = 26503);
