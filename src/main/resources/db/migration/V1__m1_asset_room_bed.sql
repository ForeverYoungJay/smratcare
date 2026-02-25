-- M1 资产与房间管理：楼栋/楼层/房间/床位 + 多租户 + 审计日志

CREATE TABLE IF NOT EXISTS building (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  name VARCHAR(64) NOT NULL COMMENT '楼栋名称',
  code VARCHAR(32) DEFAULT NULL COMMENT '楼栋编码',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
  sort_no INT NOT NULL DEFAULT 0 COMMENT '排序',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_building_tenant_name (tenant_id, name),
  UNIQUE KEY uk_building_tenant_code (tenant_id, code),
  KEY idx_building_tenant_id (tenant_id),
  KEY idx_building_org_id (org_id)
) COMMENT='楼栋';

CREATE TABLE IF NOT EXISTS floor (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  building_id BIGINT NOT NULL COMMENT '楼栋ID',
  floor_no VARCHAR(32) NOT NULL COMMENT '楼层编号',
  name VARCHAR(64) DEFAULT NULL COMMENT '楼层名称',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
  sort_no INT NOT NULL DEFAULT 0 COMMENT '排序',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_floor_tenant_building_floor (tenant_id, building_id, floor_no),
  KEY idx_floor_tenant_id (tenant_id),
  KEY idx_floor_org_id (org_id),
  KEY idx_floor_building_id (building_id)
) COMMENT='楼层';

-- 审计日志
CREATE TABLE IF NOT EXISTS audit_log (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  actor_id BIGINT DEFAULT NULL COMMENT '操作人ID',
  actor_name VARCHAR(64) DEFAULT NULL COMMENT '操作人姓名',
  action_type VARCHAR(64) NOT NULL COMMENT '动作类型',
  entity_type VARCHAR(64) NOT NULL COMMENT '实体类型',
  entity_id BIGINT DEFAULT NULL COMMENT '实体ID',
  detail VARCHAR(512) DEFAULT NULL COMMENT '详情',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY idx_audit_tenant_id (tenant_id),
  KEY idx_audit_org_id (org_id),
  KEY idx_audit_entity (entity_type, entity_id)
) COMMENT='审计日志';

-- 房间表补齐 tenant_id / 楼栋楼层ID / 二维码
ALTER TABLE room ADD COLUMN IF NOT EXISTS tenant_id BIGINT NOT NULL DEFAULT 0;
ALTER TABLE room ADD COLUMN IF NOT EXISTS building_id BIGINT DEFAULT NULL;
ALTER TABLE room ADD COLUMN IF NOT EXISTS floor_id BIGINT DEFAULT NULL;
ALTER TABLE room ADD COLUMN IF NOT EXISTS room_qr_code VARCHAR(128) DEFAULT NULL;
ALTER TABLE room ADD COLUMN IF NOT EXISTS created_by BIGINT DEFAULT NULL;

UPDATE room SET tenant_id = org_id WHERE tenant_id = 0;

CREATE INDEX IF NOT EXISTS idx_room_tenant_id ON room (tenant_id);
CREATE INDEX IF NOT EXISTS idx_room_building_id ON room (building_id);
CREATE INDEX IF NOT EXISTS idx_room_floor_id ON room (floor_id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_room_tenant_room_no ON room (tenant_id, room_no);

-- 床位表补齐 tenant_id
ALTER TABLE bed ADD COLUMN IF NOT EXISTS tenant_id BIGINT NOT NULL DEFAULT 0;
ALTER TABLE bed ADD COLUMN IF NOT EXISTS created_by BIGINT DEFAULT NULL;

UPDATE bed SET tenant_id = org_id WHERE tenant_id = 0;

CREATE INDEX IF NOT EXISTS idx_bed_tenant_id ON bed (tenant_id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_bed_tenant_qr_code ON bed (tenant_id, bed_qr_code);
