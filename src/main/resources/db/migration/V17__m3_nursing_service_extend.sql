-- M3 照护管理扩展：护理等级、服务计划、服务预定

CREATE TABLE IF NOT EXISTS care_level (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  level_code VARCHAR(32) NOT NULL COMMENT '等级编码',
  level_name VARCHAR(64) NOT NULL COMMENT '等级名称',
  severity INT NOT NULL DEFAULT 1 COMMENT '等级序号(越大越重)',
  monthly_fee DECIMAL(12,2) DEFAULT NULL COMMENT '月费参考',
  enabled TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_care_level_tenant_code (tenant_id, level_code),
  KEY idx_care_level_tenant_id (tenant_id),
  KEY idx_care_level_org_id (org_id)
) COMMENT='护理等级';

CREATE TABLE IF NOT EXISTS service_plan (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  elder_id BIGINT NOT NULL COMMENT '老人ID',
  care_level_id BIGINT DEFAULT NULL COMMENT '护理等级ID',
  service_item_id BIGINT NOT NULL COMMENT '服务项目ID',
  plan_name VARCHAR(128) NOT NULL COMMENT '计划名称',
  cycle_type VARCHAR(16) NOT NULL DEFAULT 'DAILY' COMMENT '周期 DAILY/WEEKLY/MONTHLY',
  frequency INT NOT NULL DEFAULT 1 COMMENT '频次',
  start_date DATE NOT NULL COMMENT '开始日期',
  end_date DATE DEFAULT NULL COMMENT '结束日期',
  default_staff_id BIGINT DEFAULT NULL COMMENT '默认执行人',
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态 ACTIVE/PAUSED/CLOSED',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_service_plan_tenant_id (tenant_id),
  KEY idx_service_plan_org_elder (org_id, elder_id),
  KEY idx_service_plan_status (status)
) COMMENT='服务计划';

CREATE TABLE IF NOT EXISTS service_booking (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  elder_id BIGINT NOT NULL COMMENT '老人ID',
  plan_id BIGINT DEFAULT NULL COMMENT '服务计划ID',
  service_item_id BIGINT NOT NULL COMMENT '服务项目ID',
  booking_time DATETIME NOT NULL COMMENT '预约时间',
  expected_duration INT DEFAULT NULL COMMENT '预计时长(分钟)',
  assigned_staff_id BIGINT DEFAULT NULL COMMENT '指派护工',
  source VARCHAR(16) NOT NULL DEFAULT 'MANUAL' COMMENT '来源 MANUAL/PLAN/FAMILY',
  status VARCHAR(16) NOT NULL DEFAULT 'BOOKED' COMMENT '状态 BOOKED/IN_SERVICE/DONE/CANCELLED',
  cancel_reason VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_service_booking_tenant_id (tenant_id),
  KEY idx_service_booking_org_time (org_id, booking_time),
  KEY idx_service_booking_status (status)
) COMMENT='服务预定';
