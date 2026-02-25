CREATE TABLE IF NOT EXISTS crm_callback_plan (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  lead_id BIGINT NOT NULL COMMENT '线索ID',
  title VARCHAR(128) NOT NULL COMMENT '计划标题',
  plan_execute_time DATETIME NOT NULL COMMENT '计划执行时间',
  executor_name VARCHAR(64) DEFAULT NULL COMMENT '执行人',
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '状态 PENDING/DONE',
  executed_time DATETIME DEFAULT NULL COMMENT '执行时间',
  execute_note VARCHAR(255) DEFAULT NULL COMMENT '执行备注',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_crm_callback_plan_tenant (tenant_id, is_deleted, plan_execute_time),
  KEY idx_crm_callback_plan_lead (tenant_id, lead_id, is_deleted)
) COMMENT='CRM回访计划';

CREATE TABLE IF NOT EXISTS crm_contract_attachment (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  lead_id BIGINT NOT NULL COMMENT '线索ID',
  contract_no VARCHAR(64) DEFAULT NULL COMMENT '合同编号',
  file_name VARCHAR(255) NOT NULL COMMENT '文件名',
  file_url VARCHAR(512) DEFAULT NULL COMMENT '文件链接',
  file_type VARCHAR(64) DEFAULT NULL COMMENT '文件类型',
  file_size BIGINT DEFAULT NULL COMMENT '文件大小',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_crm_contract_attachment_tenant (tenant_id, is_deleted, create_time),
  KEY idx_crm_contract_attachment_lead (tenant_id, lead_id, is_deleted)
) COMMENT='CRM合同附件';

CREATE TABLE IF NOT EXISTS crm_sms_task (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  lead_id BIGINT NOT NULL COMMENT '线索ID',
  phone VARCHAR(32) DEFAULT NULL COMMENT '手机号',
  template_name VARCHAR(64) DEFAULT NULL COMMENT '模板名称',
  content VARCHAR(255) DEFAULT NULL COMMENT '短信内容',
  plan_send_time DATETIME DEFAULT NULL COMMENT '计划发送时间',
  status VARCHAR(32) NOT NULL DEFAULT 'SCHEDULED' COMMENT '状态 SCHEDULED/SENT/FAILED',
  send_time DATETIME DEFAULT NULL COMMENT '发送时间',
  result_message VARCHAR(255) DEFAULT NULL COMMENT '发送结果',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_crm_sms_task_tenant (tenant_id, is_deleted, create_time),
  KEY idx_crm_sms_task_lead (tenant_id, lead_id, is_deleted),
  KEY idx_crm_sms_task_status (tenant_id, is_deleted, status)
) COMMENT='CRM短信任务';
