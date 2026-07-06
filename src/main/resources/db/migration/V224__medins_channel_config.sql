-- 医保对接渠道配置（适配器载体：地区/渠道差异走配置，密钥以引用方式管理，不落明文）
CREATE TABLE IF NOT EXISTS medins_channel_config (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NULL,
  org_id BIGINT NULL,
  channel VARCHAR(32) NOT NULL,
  region_code VARCHAR(32) NULL,
  endpoint VARCHAR(300) NULL,
  app_id VARCHAR(120) NULL,
  secret_ref VARCHAR(120) NULL,
  enabled TINYINT NOT NULL DEFAULT 1,
  remark VARCHAR(500) NULL,
  created_by BIGINT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_medins_channel (org_id, channel, region_code, is_deleted),
  KEY idx_medins_channel_enabled (channel, enabled)
);

-- 内置 MOCK 渠道（org_id 为 NULL 全局默认；secret_ref 指向密钥库键名，不存明文密钥）
INSERT INTO medins_channel_config (id, org_id, channel, region_code, endpoint, app_id, secret_ref, enabled, remark)
SELECT 1, NULL, 'MOCK', NULL, 'mock://medins.local/settle', 'MEDINS_MOCK_APPID', 'ref:medins.mock.secret', 1,
  '医保对接 MOCK 渠道：本地联调与验收用，正式环境按地区新增渠道配置并实现对应适配器'
WHERE NOT EXISTS (SELECT 1 FROM medins_channel_config WHERE id = 1);
