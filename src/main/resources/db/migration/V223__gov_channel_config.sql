-- 监管/医保对接渠道配置（适配器载体，字段映射与密钥引用配置化）
CREATE TABLE IF NOT EXISTS gov_channel_config (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NULL,
  org_id BIGINT NULL,
  channel VARCHAR(32) NOT NULL,
  city_code VARCHAR(32) NULL,
  endpoint VARCHAR(300) NULL,
  app_id VARCHAR(120) NULL,
  secret_ref VARCHAR(120) NULL,
  field_mapping_json JSON NULL,
  field_mapping_version VARCHAR(64) NULL,
  enabled TINYINT NOT NULL DEFAULT 1,
  remark VARCHAR(500) NULL,
  created_by BIGINT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_gov_channel (org_id, channel, city_code, is_deleted),
  KEY idx_gov_channel_enabled (channel, enabled)
);

-- 内置默认渠道配置（org_id 为 NULL，全局默认；secret_ref 指向密钥库，不落明文）
INSERT INTO gov_channel_config (id, org_id, channel, city_code, endpoint, app_id, secret_ref, field_mapping_json, field_mapping_version, enabled, remark)
SELECT 1, NULL, 'MZ_JINMIN', NULL, 'https://sandbox.mca.example/report', 'JINMIN_APPID', 'ref:jinmin.secret',
  CAST('{"elderName":"xm","idCard":"sfzh","disabilityLevel":"snjb","settleMonth":"jsny"}' AS JSON),
  'MZ_2024', 1, '民政金民工程沙箱默认配置，正式环境须替换 endpoint/密钥'
WHERE NOT EXISTS (SELECT 1 FROM gov_channel_config WHERE id = 1);

INSERT INTO gov_channel_config (id, org_id, channel, city_code, endpoint, app_id, secret_ref, field_mapping_json, field_mapping_version, enabled, remark)
SELECT 2, NULL, 'YB_MEDICAL', NULL, 'https://sandbox.ybj.example/settle', 'YB_APPID', 'ref:yb.secret',
  CAST('{"insuredNo":"ylzh","settleMonth":"jsny","fundPay":"tczf","selfPay":"grzf","totalFee":"zfy"}' AS JSON),
  'YB_2024', 1, '医保结算沙箱默认配置，正式环境须替换 endpoint/密钥'
WHERE NOT EXISTS (SELECT 1 FROM gov_channel_config WHERE id = 2);
