-- M3 照护管理扩展：护工小组、排班方案、交接班

CREATE TABLE IF NOT EXISTS caregiver_group (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  name VARCHAR(64) NOT NULL COMMENT '小组名称',
  leader_staff_id BIGINT DEFAULT NULL COMMENT '组长员工ID',
  enabled TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_caregiver_group_tenant_name (tenant_id, name),
  KEY idx_caregiver_group_tenant_id (tenant_id),
  KEY idx_caregiver_group_org_id (org_id)
) COMMENT='护工小组';

CREATE TABLE IF NOT EXISTS caregiver_group_member (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  group_id BIGINT NOT NULL COMMENT '小组ID',
  staff_id BIGINT NOT NULL COMMENT '员工ID',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_caregiver_group_member (tenant_id, group_id, staff_id),
  KEY idx_caregiver_member_group (group_id),
  KEY idx_caregiver_member_staff (staff_id)
) COMMENT='护工小组成员';

CREATE TABLE IF NOT EXISTS shift_template (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  name VARCHAR(64) NOT NULL COMMENT '方案名称',
  shift_code VARCHAR(32) NOT NULL COMMENT '班次编码',
  start_time TIME NOT NULL COMMENT '上班时间',
  end_time TIME NOT NULL COMMENT '下班时间',
  cross_day TINYINT NOT NULL DEFAULT 0 COMMENT '是否跨天 0否 1是',
  required_staff_count INT NOT NULL DEFAULT 1 COMMENT '建议人数',
  enabled TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_shift_template_tenant_code (tenant_id, shift_code),
  KEY idx_shift_template_tenant_id (tenant_id),
  KEY idx_shift_template_org_id (org_id)
) COMMENT='排班方案';

CREATE TABLE IF NOT EXISTS shift_handover (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  duty_date DATE NOT NULL COMMENT '班次日期',
  shift_code VARCHAR(32) NOT NULL COMMENT '班次编码',
  from_staff_id BIGINT NOT NULL COMMENT '交班人',
  to_staff_id BIGINT NOT NULL COMMENT '接班人',
  summary VARCHAR(1000) DEFAULT NULL COMMENT '交接摘要',
  risk_note VARCHAR(1000) DEFAULT NULL COMMENT '风险提醒',
  todo_note VARCHAR(1000) DEFAULT NULL COMMENT '待办事项',
  status VARCHAR(16) NOT NULL DEFAULT 'DRAFT' COMMENT '状态 DRAFT/HANDED_OVER/CONFIRMED',
  handover_time DATETIME DEFAULT NULL COMMENT '交班时间',
  confirm_time DATETIME DEFAULT NULL COMMENT '确认时间',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_shift_handover_tenant_id (tenant_id),
  KEY idx_shift_handover_org_date (org_id, duty_date),
  KEY idx_shift_handover_shift (shift_code),
  KEY idx_shift_handover_status (status)
) COMMENT='交接班记录';
