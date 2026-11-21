-- 数字孪生第一阶段：空间资产、模型版本、业务绑定。只新增表，不改既有业务表。
-- 当前没有 CAD/BIM/测绘，种子数据全部是「概念示意」，不代表真实园区楼栋数量、名称、用途或层数。

CREATE TABLE IF NOT EXISTS dt_scene_version (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  org_id BIGINT NOT NULL,
  version VARCHAR(32) NOT NULL,
  model_url VARCHAR(255) NULL COMMENT '为空表示没有 GLB，前端用程序化概念楼栋',
  checksum VARCHAR(128) NULL,
  source_type VARCHAR(32) NOT NULL COMMENT 'PROCEDURAL/PHOTO_REFERENCE/CAD/BIM/SURVEY',
  verification_status VARCHAR(16) NOT NULL COMMENT 'CONCEPT/UNVERIFIED/VERIFIED/SURVEYED',
  status VARCHAR(16) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/PUBLISHED/REPLACED',
  coordinate_note VARCHAR(255) NULL,
  published_at DATETIME NULL,
  replaced_by BIGINT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_dt_scene_version (org_id, version)
) COMMENT='数字孪生场景/模型版本';

CREATE TABLE IF NOT EXISTS dt_spatial_asset (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  org_id BIGINT NOT NULL,
  asset_code VARCHAR(64) NOT NULL COMMENT '稳定业务编码（对外的 assetId），换模型不变',
  name VARCHAR(128) NOT NULL,
  asset_type VARCHAR(16) NOT NULL COMMENT 'CAMPUS/BUILDING/FLOOR/ROOM/BED/DEVICE/AREA',
  parent_code VARCHAR(64) NULL,
  model_node_key VARCHAR(128) NULL COMMENT '模型节点名，可随模型版本变化',
  model_version_id BIGINT NULL,
  position_json VARCHAR(1000) NULL,
  verification_status VARCHAR(16) NOT NULL DEFAULT 'CONCEPT',
  source_reference VARCHAR(255) NULL,
  verified_by BIGINT NULL,
  verified_at DATETIME NULL,
  sort_no INT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_dt_asset_code (org_id, asset_code),
  KEY idx_dt_asset_type (org_id, asset_type)
) COMMENT='数字孪生空间资产（与模型几何分离）';

CREATE TABLE IF NOT EXISTS dt_business_binding (
  id BIGINT NOT NULL PRIMARY KEY,
  org_id BIGINT NOT NULL,
  asset_code VARCHAR(64) NOT NULL,
  source_module VARCHAR(32) NOT NULL COMMENT '首期只允许 ASSET（楼栋主数据）',
  entity_type VARCHAR(32) NOT NULL COMMENT '首期只允许 BUILDING',
  external_entity_id BIGINT NOT NULL COMMENT '既有业务表的内部 ID，严禁按名称猜测',
  binding_status VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/ACTIVE/RETIRED',
  created_by BIGINT NOT NULL,
  verified_by BIGINT NULL,
  verified_at DATETIME NULL,
  retired_at DATETIME NULL,
  remark VARCHAR(255) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_dt_binding_asset (org_id, asset_code, binding_status),
  KEY idx_dt_binding_entity (org_id, entity_type, external_entity_id, binding_status)
) COMMENT='空间资产与既有业务对象的绑定（管理员建立并发布，写审计）';

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
