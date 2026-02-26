-- Backfill legacy M4/M5/M6 tables for databases baselined at v11

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
  leave_date DATE DEFAULT NULL COMMENT '离职日期',
  leave_reason VARCHAR(255) DEFAULT NULL COMMENT '离职原因',
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

CREATE TABLE IF NOT EXISTS elder_account (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  elder_id BIGINT NOT NULL COMMENT '老人ID',
  balance DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '余额',
  credit_limit DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '信用额度',
  warn_threshold DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '余额预警阈值',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_elder_account_elder (elder_id),
  KEY idx_elder_account_org_id (org_id),
  KEY idx_elder_account_tenant_id (tenant_id)
) COMMENT='老人账户';

CREATE TABLE IF NOT EXISTS elder_account_log (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  elder_id BIGINT NOT NULL COMMENT '老人ID',
  account_id BIGINT NOT NULL COMMENT '账户ID',
  amount DECIMAL(12,2) NOT NULL COMMENT '变动金额',
  balance_after DECIMAL(12,2) NOT NULL COMMENT '变动后余额',
  direction VARCHAR(8) NOT NULL COMMENT '方向 DEBIT/CREDIT',
  source_type VARCHAR(32) NOT NULL COMMENT '来源 TASK/BILL/ADJUST/REFUND',
  source_id BIGINT DEFAULT NULL COMMENT '来源单据ID',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_elder_account_log_elder (elder_id),
  KEY idx_elder_account_log_account (account_id),
  KEY idx_elder_account_log_org (org_id)
) COMMENT='老人账户流水';

SET @charge_amount_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'care_task_template'
    AND COLUMN_NAME = 'charge_amount'
);
SET @ddl = IF(
  @charge_amount_exists = 0,
  'ALTER TABLE care_task_template ADD COLUMN charge_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT ''任务收费金额''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS meal_plan (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  plan_date DATE NOT NULL COMMENT '日期',
  meal_type VARCHAR(16) NOT NULL COMMENT '餐次 BREAKFAST/LUNCH/DINNER/SNACK',
  menu VARCHAR(512) NOT NULL COMMENT '菜单',
  calories INT DEFAULT NULL COMMENT '热量',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_meal_plan_org_date (org_id, plan_date)
) COMMENT='膳食计划';

CREATE TABLE IF NOT EXISTS activity_event (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  title VARCHAR(128) NOT NULL COMMENT '活动名称',
  event_date DATE NOT NULL COMMENT '活动日期',
  start_time DATETIME DEFAULT NULL COMMENT '开始时间',
  end_time DATETIME DEFAULT NULL COMMENT '结束时间',
  location VARCHAR(128) DEFAULT NULL COMMENT '地点',
  organizer VARCHAR(64) DEFAULT NULL COMMENT '组织者',
  content VARCHAR(512) DEFAULT NULL COMMENT '内容',
  status VARCHAR(16) NOT NULL DEFAULT 'PLANNED' COMMENT '状态 PLANNED/DONE/CANCELLED',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_activity_event_org_date (org_id, event_date)
) COMMENT='活动管理';

CREATE TABLE IF NOT EXISTS incident_report (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  elder_id BIGINT DEFAULT NULL COMMENT '老人ID',
  elder_name VARCHAR(64) DEFAULT NULL COMMENT '老人姓名',
  reporter_name VARCHAR(64) NOT NULL COMMENT '报告人',
  incident_time DATETIME NOT NULL COMMENT '发生时间',
  incident_type VARCHAR(32) NOT NULL COMMENT '事故类型',
  level VARCHAR(16) NOT NULL DEFAULT 'NORMAL' COMMENT '等级 NORMAL/MAJOR',
  description VARCHAR(512) NOT NULL COMMENT '事故描述',
  action_taken VARCHAR(512) DEFAULT NULL COMMENT '处理措施',
  status VARCHAR(16) NOT NULL DEFAULT 'OPEN' COMMENT '状态 OPEN/CLOSED',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_incident_org_time (org_id, incident_time),
  KEY idx_incident_elder (elder_id)
) COMMENT='事故登记';

CREATE TABLE IF NOT EXISTS health_basic_record (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  elder_id BIGINT NOT NULL COMMENT '老人ID',
  elder_name VARCHAR(64) DEFAULT NULL COMMENT '老人姓名',
  record_date DATE NOT NULL COMMENT '记录日期',
  height_cm DECIMAL(5,2) DEFAULT NULL COMMENT '身高(cm)',
  weight_kg DECIMAL(5,2) DEFAULT NULL COMMENT '体重(kg)',
  bmi DECIMAL(5,2) DEFAULT NULL COMMENT 'BMI',
  blood_pressure VARCHAR(16) DEFAULT NULL COMMENT '血压',
  heart_rate INT DEFAULT NULL COMMENT '心率',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_health_basic_org_date (org_id, record_date),
  KEY idx_health_basic_elder (elder_id)
) COMMENT='基础健康记录';

CREATE TABLE IF NOT EXISTS oa_notice (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  title VARCHAR(128) NOT NULL COMMENT '标题',
  content VARCHAR(2048) NOT NULL COMMENT '内容',
  publisher_name VARCHAR(64) DEFAULT NULL COMMENT '发布人',
  publish_time DATETIME DEFAULT NULL COMMENT '发布时间',
  status VARCHAR(16) NOT NULL DEFAULT 'DRAFT' COMMENT '状态 DRAFT/PUBLISHED',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_notice_org_time (org_id, publish_time)
) COMMENT='公告';

CREATE TABLE IF NOT EXISTS oa_todo (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  title VARCHAR(128) NOT NULL COMMENT '待办标题',
  content VARCHAR(512) DEFAULT NULL COMMENT '内容',
  due_time DATETIME DEFAULT NULL COMMENT '截止时间',
  status VARCHAR(16) NOT NULL DEFAULT 'OPEN' COMMENT '状态 OPEN/DONE',
  assignee_id BIGINT DEFAULT NULL COMMENT '负责人ID',
  assignee_name VARCHAR(64) DEFAULT NULL COMMENT '负责人姓名',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_todo_org_status (org_id, status)
) COMMENT='待办';

CREATE TABLE IF NOT EXISTS oa_approval (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  approval_type VARCHAR(32) NOT NULL COMMENT '类型 LEAVE/REIMBURSE/PURCHASE',
  title VARCHAR(128) NOT NULL COMMENT '标题',
  applicant_id BIGINT DEFAULT NULL COMMENT '申请人ID',
  applicant_name VARCHAR(64) NOT NULL COMMENT '申请人姓名',
  amount DECIMAL(12,2) DEFAULT NULL COMMENT '金额',
  start_time DATETIME DEFAULT NULL COMMENT '开始时间',
  end_time DATETIME DEFAULT NULL COMMENT '结束时间',
  form_data VARCHAR(2048) DEFAULT NULL COMMENT '表单数据(JSON)',
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT '状态 PENDING/APPROVED/REJECTED',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_approval_org_status (org_id, status)
) COMMENT='审批单';

CREATE TABLE IF NOT EXISTS oa_document (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  name VARCHAR(255) NOT NULL COMMENT '文件名',
  folder VARCHAR(128) DEFAULT NULL COMMENT '目录',
  url VARCHAR(512) DEFAULT NULL COMMENT '存储地址',
  size_bytes BIGINT DEFAULT NULL COMMENT '大小',
  uploader_id BIGINT DEFAULT NULL COMMENT '上传人ID',
  uploader_name VARCHAR(64) DEFAULT NULL COMMENT '上传人姓名',
  uploaded_at DATETIME DEFAULT NULL COMMENT '上传时间',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_document_org_folder (org_id, folder)
) COMMENT='文档管理';

CREATE TABLE IF NOT EXISTS oa_task (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  title VARCHAR(128) NOT NULL COMMENT '任务标题',
  description VARCHAR(512) DEFAULT NULL COMMENT '描述',
  start_time DATETIME DEFAULT NULL COMMENT '开始时间',
  end_time DATETIME DEFAULT NULL COMMENT '结束时间',
  priority VARCHAR(16) NOT NULL DEFAULT 'NORMAL' COMMENT '优先级 LOW/NORMAL/HIGH',
  status VARCHAR(16) NOT NULL DEFAULT 'OPEN' COMMENT '状态 OPEN/DONE',
  assignee_id BIGINT DEFAULT NULL COMMENT '负责人ID',
  assignee_name VARCHAR(64) DEFAULT NULL COMMENT '负责人姓名',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_oa_task_org_time (org_id, start_time),
  KEY idx_oa_task_org_status (org_id, status)
) COMMENT='日程与任务';

CREATE TABLE IF NOT EXISTS staff_schedule (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  staff_id BIGINT NOT NULL COMMENT '员工ID',
  duty_date DATE NOT NULL COMMENT '值班日期',
  shift_code VARCHAR(32) DEFAULT NULL COMMENT '班次',
  start_time DATETIME DEFAULT NULL COMMENT '开始时间',
  end_time DATETIME DEFAULT NULL COMMENT '结束时间',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1正常 0停用',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_staff_schedule_tenant_id (tenant_id),
  KEY idx_staff_schedule_org_id (org_id),
  KEY idx_staff_schedule_staff_date (staff_id, duty_date)
) COMMENT='员工日程';

CREATE TABLE IF NOT EXISTS attendance_record (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  staff_id BIGINT NOT NULL COMMENT '员工ID',
  check_in_time DATETIME DEFAULT NULL COMMENT '签到时间',
  check_out_time DATETIME DEFAULT NULL COMMENT '签退时间',
  status VARCHAR(32) DEFAULT NULL COMMENT '考勤状态',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_attendance_tenant_id (tenant_id),
  KEY idx_attendance_org_id (org_id),
  KEY idx_attendance_staff_time (staff_id, check_in_time)
) COMMENT='考勤记录';

CREATE TABLE IF NOT EXISTS report_record (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  name VARCHAR(128) NOT NULL COMMENT '报表名称',
  generated_at DATETIME NOT NULL COMMENT '生成时间',
  url VARCHAR(255) DEFAULT NULL COMMENT '下载地址',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_report_tenant_id (tenant_id),
  KEY idx_report_org_id (org_id)
) COMMENT='报表记录';

UPDATE staff_profile SET tenant_id = org_id WHERE tenant_id = 0;
UPDATE staff_training_record SET tenant_id = org_id WHERE tenant_id = 0;
UPDATE staff_points_account SET tenant_id = org_id WHERE tenant_id = 0;
UPDATE staff_points_log SET tenant_id = org_id WHERE tenant_id = 0;
