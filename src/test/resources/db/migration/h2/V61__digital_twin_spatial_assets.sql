-- 与 MySQL V298 对应；building 在 H2 测试库中缺失，这里补一份与生产一致的结构
CREATE TABLE IF NOT EXISTS building (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NULL,
  org_id BIGINT NULL,
  name VARCHAR(64) NOT NULL,
  code VARCHAR(64) NULL,
  area_code VARCHAR(64) NULL,
  area_name VARCHAR(128) NULL,
  status TINYINT NOT NULL DEFAULT 1,
  sort_no INT NOT NULL DEFAULT 0,
  remark VARCHAR(255) NULL,
  created_by BIGINT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS dt_scene_version (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  org_id BIGINT NOT NULL,
  version VARCHAR(32) NOT NULL,
  model_url VARCHAR(255) NULL,
  checksum VARCHAR(128) NULL,
  source_type VARCHAR(32) NOT NULL,
  verification_status VARCHAR(16) NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
  coordinate_note VARCHAR(255) NULL,
  published_at DATETIME NULL,
  replaced_by BIGINT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_dt_scene_version UNIQUE (org_id, version)
);

CREATE TABLE IF NOT EXISTS dt_spatial_asset (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  org_id BIGINT NOT NULL,
  asset_code VARCHAR(64) NOT NULL,
  name VARCHAR(128) NOT NULL,
  asset_type VARCHAR(16) NOT NULL,
  parent_code VARCHAR(64) NULL,
  model_node_key VARCHAR(128) NULL,
  model_version_id BIGINT NULL,
  position_json VARCHAR(1000) NULL,
  verification_status VARCHAR(16) NOT NULL DEFAULT 'CONCEPT',
  source_reference VARCHAR(255) NULL,
  verified_by BIGINT NULL,
  verified_at DATETIME NULL,
  sort_no INT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_dt_asset_code UNIQUE (org_id, asset_code)
);

CREATE TABLE IF NOT EXISTS dt_business_binding (
  id BIGINT NOT NULL PRIMARY KEY,
  org_id BIGINT NOT NULL,
  asset_code VARCHAR(64) NOT NULL,
  source_module VARCHAR(32) NOT NULL,
  entity_type VARCHAR(32) NOT NULL,
  external_entity_id BIGINT NOT NULL,
  binding_status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  created_by BIGINT NOT NULL,
  verified_by BIGINT NULL,
  verified_at DATETIME NULL,
  retired_at DATETIME NULL,
  remark VARCHAR(255) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO dt_scene_version (org_id, version, model_url, source_type, verification_status, status, coordinate_note, published_at)
SELECT o.id, '0.1.0-concept', NULL, 'PROCEDURAL', 'CONCEPT', 'PUBLISHED',
       '局部示意坐标，单位不具测绘精度，Y 轴朝上', CURRENT_TIMESTAMP
FROM org o WHERE o.is_deleted = 0;

INSERT INTO dt_spatial_asset (org_id, asset_code, name, asset_type, parent_code, model_node_key, model_version_id, verification_status, source_reference, sort_no)
SELECT v.org_id, 'GF-CAMPUS-001', '园区（概念示意）', 'CAMPUS', NULL, 'CAMPUS_GROUND', v.id, 'CONCEPT', '规划鸟瞰效果图、航拍拼图（仅外观参考）', 0
FROM dt_scene_version v WHERE v.version = '0.1.0-concept';

INSERT INTO dt_spatial_asset (org_id, asset_code, name, asset_type, parent_code, model_node_key, model_version_id, verification_status, source_reference, sort_no)
SELECT v.org_id, CONCAT('GF-BLD-TMP-', LPAD(n.n, 3, '0')), CONCAT('示例楼栋', LPAD(n.n, 2, '0'), '（待核实）'), 'BUILDING',
       'GF-CAMPUS-001', CONCAT('BLD_TMP_', LPAD(n.n, 3, '0')), v.id, 'CONCEPT', '规划鸟瞰效果图、航拍拼图（仅外观参考）', n.n
FROM dt_scene_version v
JOIN (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
      UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) n
WHERE v.version = '0.1.0-concept';
