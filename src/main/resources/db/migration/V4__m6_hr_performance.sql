-- M6 人力资源与绩效

CREATE TABLE IF NOT EXISTS staff_profile (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  staff_id BIGINT NOT NULL COMMENT '员工ID',
  job_title VARCHAR(64) DEFAULT NULL COMMENT '岗位/职称',
  employment_type VARCHAR(32) DEFAULT NULL COMMENT '用工类型',
  hire_date DATE DEFAULT NULL COMMENT '入职日期',
  qualification_level VARCHAR(32) DEFAULT NULL COMMENT '资质等级',
  certificate_no VARCHAR(64) DEFAULT NULL COMMENT '证书编号',
  emergency_contact_name VARCHAR(64) DEFAULT NULL COMMENT '紧急联系人',
  emergency_contact_phone VARCHAR(32) DEFAULT NULL COMMENT '紧急联系电话',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1在职 0离职',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_staff_profile (org_id, staff_id),
  KEY idx_staff_profile_tenant_id (tenant_id),
  KEY idx_staff_profile_staff_id (staff_id)
) COMMENT='员工档案扩展';

CREATE TABLE IF NOT EXISTS staff_training_record (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  staff_id BIGINT NOT NULL COMMENT '员工ID',
  training_name VARCHAR(128) NOT NULL COMMENT '培训名称',
  training_type VARCHAR(32) DEFAULT NULL COMMENT '培训类型',
  provider VARCHAR(128) DEFAULT NULL COMMENT '培训机构/讲师',
  start_date DATE DEFAULT NULL COMMENT '开始日期',
  end_date DATE DEFAULT NULL COMMENT '结束日期',
  hours DECIMAL(6,2) DEFAULT NULL COMMENT '培训时长(小时)',
  score DECIMAL(5,2) DEFAULT NULL COMMENT '培训成绩',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1完成 0未完成',
  certificate_no VARCHAR(64) DEFAULT NULL COMMENT '证书编号',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_staff_training_tenant_id (tenant_id),
  KEY idx_staff_training_staff_id (staff_id),
  KEY idx_staff_training_org_id (org_id)
) COMMENT='员工培训记录';

CREATE TABLE IF NOT EXISTS staff_points_account (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  staff_id BIGINT NOT NULL COMMENT '员工ID',
  points_balance INT NOT NULL DEFAULT 0 COMMENT '积分余额',
  total_earned INT NOT NULL DEFAULT 0 COMMENT '累计获得积分',
  total_deducted INT NOT NULL DEFAULT 0 COMMENT '累计扣减积分',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1正常 0冻结',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_staff_points_account (org_id, staff_id),
  KEY idx_staff_points_account_tenant_id (tenant_id),
  KEY idx_staff_points_account_staff_id (staff_id)
) COMMENT='员工积分账户';

CREATE TABLE IF NOT EXISTS staff_points_log (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  staff_id BIGINT NOT NULL COMMENT '员工ID',
  change_type VARCHAR(32) NOT NULL COMMENT '变更类型 EARN/DEDUCT/ADJUST',
  change_points INT NOT NULL COMMENT '变更积分(可为负)',
  balance_after INT NOT NULL COMMENT '变更后余额',
  source_type VARCHAR(32) DEFAULT NULL COMMENT '来源 TASK/REVIEW/MANUAL',
  source_id BIGINT DEFAULT NULL COMMENT '来源ID',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_staff_points_log_tenant_id (tenant_id),
  KEY idx_staff_points_log_staff_id (staff_id),
  KEY idx_staff_points_log_source (source_type, source_id)
) COMMENT='员工积分流水';

UPDATE staff_profile SET tenant_id = org_id WHERE tenant_id = 0;
UPDATE staff_training_record SET tenant_id = org_id WHERE tenant_id = 0;
UPDATE staff_points_account SET tenant_id = org_id WHERE tenant_id = 0;
UPDATE staff_points_log SET tenant_id = org_id WHERE tenant_id = 0;
