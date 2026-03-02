ALTER TABLE crm_callback_plan
  ADD COLUMN followup_content VARCHAR(500) DEFAULT NULL COMMENT '回访内容' AFTER title,
  ADD COLUMN followup_result VARCHAR(500) DEFAULT NULL COMMENT '回访结果' AFTER execute_note;

CREATE TABLE IF NOT EXISTS crm_marketing_plan_department (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  plan_id BIGINT NOT NULL COMMENT '营销方案ID',
  department_id BIGINT NOT NULL COMMENT '部门ID',
  department_name VARCHAR(64) DEFAULT NULL COMMENT '部门名称',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_crm_plan_department_plan (org_id, plan_id, is_deleted),
  KEY idx_crm_plan_department_dept (org_id, department_id, is_deleted)
) COMMENT='营销方案联动部门';

CREATE TABLE IF NOT EXISTS crm_marketing_plan_approval (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  plan_id BIGINT NOT NULL COMMENT '营销方案ID',
  approver_id BIGINT DEFAULT NULL COMMENT '审批人ID',
  approver_name VARCHAR(64) DEFAULT NULL COMMENT '审批人',
  status VARCHAR(16) NOT NULL COMMENT '状态 APPROVED/REJECTED',
  remark VARCHAR(500) DEFAULT NULL COMMENT '审批意见',
  approved_time DATETIME DEFAULT NULL COMMENT '审批时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_crm_plan_approval_plan (org_id, plan_id, is_deleted, create_time)
) COMMENT='营销方案审批记录';

CREATE TABLE IF NOT EXISTS crm_marketing_plan_receipt (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  plan_id BIGINT NOT NULL COMMENT '营销方案ID',
  staff_id BIGINT NOT NULL COMMENT '员工ID',
  staff_name VARCHAR(64) DEFAULT NULL COMMENT '员工姓名',
  read_time DATETIME DEFAULT NULL COMMENT '阅读时间',
  action VARCHAR(16) DEFAULT NULL COMMENT '动作 AGREE/IMPROVE',
  action_detail VARCHAR(1000) DEFAULT NULL COMMENT '改进说明',
  action_time DATETIME DEFAULT NULL COMMENT '动作时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_crm_plan_receipt_plan (org_id, plan_id, is_deleted),
  KEY idx_crm_plan_receipt_staff (org_id, staff_id, is_deleted),
  UNIQUE KEY uk_crm_plan_receipt_staff (plan_id, staff_id, is_deleted)
) COMMENT='营销方案阅读回执';

CREATE TABLE IF NOT EXISTS crm_marketing_plan_performance (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  plan_id BIGINT NOT NULL COMMENT '营销方案ID',
  staff_id BIGINT NOT NULL COMMENT '员工ID',
  staff_name VARCHAR(64) DEFAULT NULL COMMENT '员工姓名',
  score DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '绩效分值',
  metric VARCHAR(64) DEFAULT NULL COMMENT '指标编码',
  source VARCHAR(32) DEFAULT NULL COMMENT '来源 MARKETING_PLAN',
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT '状态 PENDING/CONFIRMED',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_crm_plan_performance_plan (org_id, plan_id, is_deleted),
  KEY idx_crm_plan_performance_staff (org_id, staff_id, is_deleted),
  UNIQUE KEY uk_crm_plan_performance_staff (plan_id, staff_id, is_deleted)
) COMMENT='营销方案绩效联动';
