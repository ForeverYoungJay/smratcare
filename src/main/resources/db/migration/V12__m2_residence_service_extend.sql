-- M2 入住服务增强：外出登记/试住登记/退住申请（基线）

CREATE TABLE IF NOT EXISTS elder_outing_record (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  elder_id BIGINT NOT NULL COMMENT '老人ID',
  elder_name VARCHAR(64) DEFAULT NULL COMMENT '老人姓名',
  outing_date DATE NOT NULL COMMENT '外出日期',
  expected_return_time DATETIME DEFAULT NULL COMMENT '预计返院时间',
  actual_return_time DATETIME DEFAULT NULL COMMENT '实际返院时间',
  companion VARCHAR(64) DEFAULT NULL COMMENT '陪同人',
  reason VARCHAR(255) DEFAULT NULL COMMENT '外出事由',
  status VARCHAR(16) NOT NULL DEFAULT 'OUT' COMMENT '状态 OUT/RETURNED',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_outing_org_date (org_id, outing_date),
  KEY idx_outing_elder (elder_id),
  KEY idx_outing_status (status)
) COMMENT='老人外出登记';

CREATE TABLE IF NOT EXISTS elder_trial_stay (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  elder_id BIGINT NOT NULL COMMENT '老人ID',
  elder_name VARCHAR(64) DEFAULT NULL COMMENT '老人姓名',
  trial_start_date DATE NOT NULL COMMENT '试住开始日期',
  trial_end_date DATE NOT NULL COMMENT '试住结束日期',
  channel VARCHAR(32) DEFAULT NULL COMMENT '来源渠道',
  intent_level VARCHAR(16) DEFAULT NULL COMMENT '意向等级 HIGH/MEDIUM/LOW',
  status VARCHAR(16) NOT NULL DEFAULT 'REGISTERED' COMMENT '状态 REGISTERED/FINISHED/CONVERTED/CANCELLED',
  care_level VARCHAR(32) DEFAULT NULL COMMENT '试住护理等级',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_trial_org_date (org_id, trial_start_date),
  KEY idx_trial_elder (elder_id),
  KEY idx_trial_status (status)
) COMMENT='老人试住登记';

CREATE TABLE IF NOT EXISTS elder_discharge_apply (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  elder_id BIGINT NOT NULL COMMENT '老人ID',
  elder_name VARCHAR(64) DEFAULT NULL COMMENT '老人姓名',
  apply_date DATE NOT NULL COMMENT '申请日期',
  planned_discharge_date DATE NOT NULL COMMENT '计划退住日期',
  reason VARCHAR(255) NOT NULL COMMENT '申请原因',
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT '状态 PENDING/APPROVED/REJECTED',
  review_remark VARCHAR(255) DEFAULT NULL COMMENT '审核备注',
  reviewed_by BIGINT DEFAULT NULL COMMENT '审核人',
  reviewed_time DATETIME DEFAULT NULL COMMENT '审核时间',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_discharge_apply_org_date (org_id, apply_date),
  KEY idx_discharge_apply_elder (elder_id),
  KEY idx_discharge_apply_status (status)
) COMMENT='老人退住申请';
