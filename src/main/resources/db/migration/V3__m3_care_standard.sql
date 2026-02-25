-- M3 服务与护理标准化

-- care_task_template / daily / execute_log / review 补齐 tenant_id
ALTER TABLE care_task_template ADD COLUMN IF NOT EXISTS tenant_id BIGINT NOT NULL DEFAULT 0;
ALTER TABLE care_task_template ADD COLUMN IF NOT EXISTS created_by BIGINT DEFAULT NULL;

UPDATE care_task_template SET tenant_id = org_id WHERE tenant_id = 0;

CREATE INDEX IF NOT EXISTS idx_care_task_template_tenant_id ON care_task_template (tenant_id);

ALTER TABLE care_task_daily ADD COLUMN IF NOT EXISTS tenant_id BIGINT NOT NULL DEFAULT 0;
ALTER TABLE care_task_daily ADD COLUMN IF NOT EXISTS source_type VARCHAR(32) DEFAULT NULL;
ALTER TABLE care_task_daily ADD COLUMN IF NOT EXISTS source_id BIGINT DEFAULT NULL;
ALTER TABLE care_task_daily ADD COLUMN IF NOT EXISTS created_by BIGINT DEFAULT NULL;

UPDATE care_task_daily SET tenant_id = org_id WHERE tenant_id = 0;

CREATE INDEX IF NOT EXISTS idx_care_task_daily_tenant_id ON care_task_daily (tenant_id);
CREATE INDEX IF NOT EXISTS idx_care_task_daily_source ON care_task_daily (source_type, source_id);

ALTER TABLE care_task_execute_log ADD COLUMN IF NOT EXISTS tenant_id BIGINT NOT NULL DEFAULT 0;

UPDATE care_task_execute_log SET tenant_id = org_id WHERE tenant_id = 0;

CREATE INDEX IF NOT EXISTS idx_care_task_execute_tenant_id ON care_task_execute_log (tenant_id);

ALTER TABLE care_task_review ADD COLUMN IF NOT EXISTS tenant_id BIGINT NOT NULL DEFAULT 0;

UPDATE care_task_review SET tenant_id = org_id WHERE tenant_id = 0;

CREATE INDEX IF NOT EXISTS idx_care_task_review_tenant_id ON care_task_review (tenant_id);

-- 服务项目库
CREATE TABLE IF NOT EXISTS service_item (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  name VARCHAR(128) NOT NULL COMMENT '服务名称',
  category VARCHAR(64) DEFAULT NULL COMMENT '分类',
  default_duration INT DEFAULT NULL COMMENT '默认时长(分钟)',
  default_points INT DEFAULT NULL COMMENT '默认积分/费用',
  enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_service_item_tenant_name (tenant_id, name),
  KEY idx_service_item_tenant_id (tenant_id),
  KEY idx_service_item_org_id (org_id)
) COMMENT='服务项目库';

-- 护理套餐
CREATE TABLE IF NOT EXISTS care_package (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  name VARCHAR(128) NOT NULL COMMENT '套餐名称',
  care_level VARCHAR(32) DEFAULT NULL COMMENT '护理等级',
  cycle_days INT NOT NULL DEFAULT 1 COMMENT '周期天数',
  enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_care_package_tenant_name (tenant_id, name),
  KEY idx_care_package_tenant_id (tenant_id),
  KEY idx_care_package_org_id (org_id)
) COMMENT='护理套餐';

-- 套餐明细
CREATE TABLE IF NOT EXISTS care_package_item (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  package_id BIGINT NOT NULL COMMENT '套餐ID',
  item_id BIGINT NOT NULL COMMENT '服务项ID',
  frequency_per_day INT NOT NULL DEFAULT 1 COMMENT '每日频次',
  enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
  sort_no INT NOT NULL DEFAULT 0 COMMENT '排序',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_package_item_tenant_package_item (tenant_id, package_id, item_id),
  KEY idx_package_item_tenant_id (tenant_id),
  KEY idx_package_item_package_id (package_id)
) COMMENT='套餐明细';

-- 老人套餐
CREATE TABLE IF NOT EXISTS elder_package (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  elder_id BIGINT NOT NULL COMMENT '老人ID',
  package_id BIGINT NOT NULL COMMENT '套餐ID',
  start_date DATE NOT NULL COMMENT '开始日期',
  end_date DATE DEFAULT NULL COMMENT '结束日期',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_elder_package_tenant_id (tenant_id),
  KEY idx_elder_package_elder_id (elder_id),
  KEY idx_elder_package_package_id (package_id)
) COMMENT='老人套餐';
