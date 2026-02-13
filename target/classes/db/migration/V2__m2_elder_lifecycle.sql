-- M2 长者全周期管理：CRM、入院办理、变更、退院

-- elder 补齐租户与审计字段
ALTER TABLE elder ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 0;
ALTER TABLE elder ADD COLUMN created_by BIGINT DEFAULT NULL;

UPDATE elder SET tenant_id = org_id WHERE tenant_id = 0;

CREATE INDEX idx_elder_tenant_id ON elder (tenant_id);

-- elder_bed_relation 补齐租户与审计字段
ALTER TABLE elder_bed_relation ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 0;
ALTER TABLE elder_bed_relation ADD COLUMN created_by BIGINT DEFAULT NULL;

UPDATE elder_bed_relation SET tenant_id = org_id WHERE tenant_id = 0;

CREATE INDEX idx_elder_bed_rel_tenant_id ON elder_bed_relation (tenant_id);

-- CRM 线索
CREATE TABLE IF NOT EXISTS crm_lead (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  name VARCHAR(64) NOT NULL COMMENT '姓名',
  phone VARCHAR(32) DEFAULT NULL COMMENT '电话',
  source VARCHAR(64) DEFAULT NULL COMMENT '来源',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0新建 1跟进中 2已签约 3流失',
  next_follow_date DATE DEFAULT NULL COMMENT '下次跟进日期',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_crm_lead_tenant_id (tenant_id),
  KEY idx_crm_lead_org_id (org_id),
  KEY idx_crm_lead_status (status)
) COMMENT='CRM线索';

-- 入院办理记录
CREATE TABLE IF NOT EXISTS elder_admission (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  elder_id BIGINT NOT NULL COMMENT '老人ID',
  admission_date DATE NOT NULL COMMENT '入院日期',
  contract_no VARCHAR(64) DEFAULT NULL COMMENT '合同号',
  deposit_amount DECIMAL(12,2) DEFAULT NULL COMMENT '押金金额',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_admission_tenant_id (tenant_id),
  KEY idx_admission_org_id (org_id),
  KEY idx_admission_elder_id (elder_id)
) COMMENT='入院办理';

-- 变更记录
CREATE TABLE IF NOT EXISTS elder_change_log (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  elder_id BIGINT NOT NULL COMMENT '老人ID',
  change_type VARCHAR(32) NOT NULL COMMENT '变更类型 BED_CHANGE/CARE_LEVEL/STATUS/ROOM',
  before_value VARCHAR(128) DEFAULT NULL COMMENT '变更前',
  after_value VARCHAR(128) DEFAULT NULL COMMENT '变更后',
  reason VARCHAR(255) DEFAULT NULL COMMENT '原因',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY idx_change_tenant_id (tenant_id),
  KEY idx_change_org_id (org_id),
  KEY idx_change_elder_id (elder_id)
) COMMENT='长者变更记录';

-- 退院记录
CREATE TABLE IF NOT EXISTS elder_discharge (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  elder_id BIGINT NOT NULL COMMENT '老人ID',
  discharge_date DATE NOT NULL COMMENT '退院日期',
  reason VARCHAR(255) DEFAULT NULL COMMENT '原因',
  settle_amount DECIMAL(12,2) DEFAULT NULL COMMENT '结算金额',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_discharge_tenant_id (tenant_id),
  KEY idx_discharge_org_id (org_id),
  KEY idx_discharge_elder_id (elder_id)
) COMMENT='退院记录';
