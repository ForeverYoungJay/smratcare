-- 智养云智慧养老OA平台（纯SaaS）MySQL 8 数据库表结构
-- 多机构隔离：所有业务表均包含 org_id

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 组织
CREATE TABLE org (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_code VARCHAR(64) NOT NULL COMMENT '机构编码',
  org_name VARCHAR(128) NOT NULL COMMENT '机构名称',
  org_type VARCHAR(32) NOT NULL COMMENT '机构类型',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
  contact_name VARCHAR(64) DEFAULT NULL COMMENT '联系人',
  contact_phone VARCHAR(32) DEFAULT NULL COMMENT '联系电话',
  address VARCHAR(255) DEFAULT NULL COMMENT '地址',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_org_code (org_code),
  KEY idx_org_name (org_name)
) COMMENT='养老机构';

-- 部门
CREATE TABLE department (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  parent_id BIGINT DEFAULT NULL COMMENT '上级部门ID',
  dept_name VARCHAR(128) NOT NULL COMMENT '部门名称',
  dept_code VARCHAR(64) DEFAULT NULL COMMENT '部门编码',
  sort_no INT NOT NULL DEFAULT 0 COMMENT '排序号',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_department_org_id (org_id),
  KEY idx_department_parent_id (parent_id)
) COMMENT='部门';

-- 角色
CREATE TABLE role (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  role_name VARCHAR(128) NOT NULL COMMENT '角色名称',
  role_code VARCHAR(64) NOT NULL COMMENT '角色编码',
  role_desc VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_role_org_code (org_id, role_code),
  KEY idx_role_name (role_name)
) COMMENT='角色';

-- 员工账号
CREATE TABLE staff (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  department_id BIGINT NOT NULL COMMENT '部门ID',
  staff_no VARCHAR(64) NOT NULL COMMENT '员工编号',
  username VARCHAR(64) NOT NULL COMMENT '登录账号',
  password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
  real_name VARCHAR(64) NOT NULL COMMENT '姓名',
  phone VARCHAR(32) DEFAULT NULL COMMENT '手机号',
  email VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
  gender TINYINT DEFAULT NULL COMMENT '性别 1男 2女 0未知',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
  last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_staff_org_username (org_id, username),
  UNIQUE KEY uk_staff_org_staff_no (org_id, staff_no),
  KEY idx_staff_org_id (org_id),
  KEY idx_staff_department_id (department_id),
  KEY idx_staff_phone (phone)
) COMMENT='员工账号';

-- 员工角色关联
CREATE TABLE staff_role (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  staff_id BIGINT NOT NULL COMMENT '员工ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_staff_role (org_id, staff_id, role_id),
  KEY idx_staff_role_staff_id (staff_id),
  KEY idx_staff_role_role_id (role_id)
) COMMENT='员工角色关联';

-- 老人档案
CREATE TABLE elder (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  elder_code VARCHAR(64) NOT NULL COMMENT '老人编号',
  elder_qr_code VARCHAR(128) NOT NULL COMMENT '老人二维码字符串',
  full_name VARCHAR(64) NOT NULL COMMENT '姓名',
  id_card_no VARCHAR(32) DEFAULT NULL COMMENT '身份证号',
  gender TINYINT DEFAULT NULL COMMENT '性别 1男 2女 0未知',
  birth_date DATE DEFAULT NULL COMMENT '出生日期',
  phone VARCHAR(32) DEFAULT NULL COMMENT '联系电话',
  admission_date DATE DEFAULT NULL COMMENT '入院日期',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1在院 2请假 3离院',
  bed_id BIGINT DEFAULT NULL COMMENT '床位ID',
  care_level VARCHAR(32) DEFAULT NULL COMMENT '护理等级',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_elder_org_code (org_id, elder_code),
  UNIQUE KEY uk_elder_org_qr (org_id, elder_qr_code),
  KEY idx_elder_org_id (org_id),
  KEY idx_elder_bed_id (bed_id),
  KEY idx_elder_id_card_no (id_card_no)
) COMMENT='老人档案';

-- 家属账号（App/小程序）
CREATE TABLE family_user (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  open_id VARCHAR(128) DEFAULT NULL COMMENT '第三方OpenID',
  username VARCHAR(64) DEFAULT NULL COMMENT '登录账号',
  password_hash VARCHAR(255) DEFAULT NULL COMMENT '密码哈希',
  real_name VARCHAR(64) NOT NULL COMMENT '姓名',
  phone VARCHAR(32) NOT NULL COMMENT '手机号',
  id_card_no VARCHAR(32) DEFAULT NULL COMMENT '身份证号',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_family_org_phone (org_id, phone),
  KEY idx_family_org_id (org_id)
) COMMENT='家属账号';

-- 老人-家属关系
CREATE TABLE elder_family (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  elder_id BIGINT NOT NULL COMMENT '老人ID',
  family_user_id BIGINT NOT NULL COMMENT '家属账号ID',
  relation VARCHAR(32) NOT NULL COMMENT '关系 如子女/配偶',
  is_primary TINYINT NOT NULL DEFAULT 0 COMMENT '是否主要联系人',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_elder_family (org_id, elder_id, family_user_id),
  KEY idx_elder_family_elder_id (elder_id),
  KEY idx_elder_family_family_id (family_user_id)
) COMMENT='老人-家属关系';

-- 老人床位关系（历史）
CREATE TABLE elder_bed_relation (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  elder_id BIGINT NOT NULL COMMENT '老人ID',
  bed_id BIGINT NOT NULL COMMENT '床位ID',
  start_date DATE NOT NULL COMMENT '开始日期',
  end_date DATE DEFAULT NULL COMMENT '结束日期',
  active_flag TINYINT NOT NULL DEFAULT 1 COMMENT '当前有效 1是 0否',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_elder_bed_rel_elder_id (elder_id),
  KEY idx_elder_bed_rel_bed_id (bed_id),
  KEY idx_elder_bed_rel_active (active_flag)
) COMMENT='老人床位关系';

-- 房间
CREATE TABLE room (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  building VARCHAR(64) DEFAULT NULL COMMENT '楼栋',
  floor_no VARCHAR(32) DEFAULT NULL COMMENT '楼层',
  room_no VARCHAR(32) NOT NULL COMMENT '房间号',
  room_type VARCHAR(32) DEFAULT NULL COMMENT '房间类型',
  capacity INT NOT NULL DEFAULT 1 COMMENT '床位容量',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1可用 0停用',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_room_org_room_no (org_id, room_no),
  KEY idx_room_org_id (org_id)
) COMMENT='房间';

-- 床位
CREATE TABLE bed (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  room_id BIGINT NOT NULL COMMENT '房间ID',
  bed_no VARCHAR(32) NOT NULL COMMENT '床位号',
  bed_qr_code VARCHAR(128) NOT NULL COMMENT '床位二维码字符串',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1空闲 2占用 3维修',
  elder_id BIGINT DEFAULT NULL COMMENT '当前老人ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_bed_org_room_bed_no (org_id, room_id, bed_no),
  UNIQUE KEY uk_bed_org_qr_code (org_id, bed_qr_code),
  KEY idx_bed_org_id (org_id),
  KEY idx_bed_room_id (room_id),
  KEY idx_bed_elder_id (elder_id)
) COMMENT='床位';

-- 护理任务模板
CREATE TABLE care_task_template (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  task_name VARCHAR(128) NOT NULL COMMENT '任务名称',
  frequency_per_day INT NOT NULL DEFAULT 1 COMMENT '每日频次',
  care_level_required VARCHAR(32) DEFAULT NULL COMMENT '护理等级要求',
  enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_care_task_template_task_name (task_name),
  KEY idx_care_task_template_org_id (org_id)
) COMMENT='护理任务模板';

-- 护理任务（日任务）
CREATE TABLE care_task_daily (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  elder_id BIGINT NOT NULL COMMENT '老人ID',
  bed_id BIGINT DEFAULT NULL COMMENT '床位ID',
  template_id BIGINT NOT NULL COMMENT '模板ID',
  task_date DATE NOT NULL COMMENT '任务日期',
  plan_time DATETIME DEFAULT NULL COMMENT '计划执行时间',
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT '任务状态 PENDING/DONE/EXCEPTION',
  assigned_staff_id BIGINT DEFAULT NULL COMMENT '指派员工ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_care_task_daily_elder_id (elder_id),
  KEY idx_care_task_daily_bed_id (bed_id),
  KEY idx_care_task_daily_task_date (task_date),
  KEY idx_care_task_daily_template_id (template_id),
  KEY idx_care_task_daily_staff_id (assigned_staff_id),
  KEY idx_care_task_daily_status (status)
) COMMENT='护理日任务';

-- 护理任务执行日志
CREATE TABLE care_task_execute_log (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  task_daily_id BIGINT NOT NULL COMMENT '日任务ID',
  elder_id BIGINT NOT NULL COMMENT '老人ID',
  bed_id BIGINT DEFAULT NULL COMMENT '床位ID',
  staff_id BIGINT NOT NULL COMMENT '执行员工ID',
  execute_time DATETIME NOT NULL COMMENT '执行时间',
  bed_qr_code VARCHAR(128) NOT NULL COMMENT '床位二维码',
  result_status TINYINT NOT NULL DEFAULT 1 COMMENT '结果 1成功 0失败',
  suspicious_flag TINYINT NOT NULL DEFAULT 0 COMMENT '可疑标记 1是 0否',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_care_task_execute_task_daily_id (task_daily_id),
  KEY idx_care_task_execute_elder_id (elder_id),
  KEY idx_care_task_execute_bed_id (bed_id),
  KEY idx_care_task_execute_staff_id (staff_id),
  KEY idx_care_task_execute_time (execute_time)
) COMMENT='护理任务执行日志';

-- 生命体征记录
CREATE TABLE vital_sign_record (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  elder_id BIGINT NOT NULL COMMENT '老人ID',
  type VARCHAR(16) NOT NULL COMMENT '类型 BP/BS/TEMP/SPO2/HR',
  value_json VARCHAR(512) NOT NULL COMMENT '测量值JSON',
  measured_at DATETIME NOT NULL COMMENT '测量时间',
  recorded_by_staff_id BIGINT DEFAULT NULL COMMENT '录入员工ID',
  abnormal_flag TINYINT NOT NULL DEFAULT 0 COMMENT '异常标记 1是 0否',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_vital_sign_elder_id (elder_id),
  KEY idx_vital_sign_type (type),
  KEY idx_vital_sign_measured_at (measured_at),
  KEY idx_vital_sign_abnormal (abnormal_flag)
) COMMENT='生命体征记录';

-- 生命体征阈值配置
CREATE TABLE vital_threshold_config (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  type VARCHAR(16) NOT NULL COMMENT '类型 BP/BS/TEMP/SPO2/HR',
  metric_code VARCHAR(32) DEFAULT NULL COMMENT '指标代码 如SBP/DBP',
  min_value DECIMAL(10,2) DEFAULT NULL COMMENT '下限',
  max_value DECIMAL(10,2) DEFAULT NULL COMMENT '上限',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_vital_threshold_org (org_id),
  KEY idx_vital_threshold_type (type),
  KEY idx_vital_threshold_metric (metric_code)
) COMMENT='生命体征阈值配置';

-- 疾病
CREATE TABLE disease (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  disease_code VARCHAR(64) NOT NULL COMMENT '疾病编码',
  disease_name VARCHAR(128) NOT NULL COMMENT '疾病名称',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_disease_org_code (org_id, disease_code),
  KEY idx_disease_name (disease_name)
) COMMENT='疾病';

-- 老人疾病
CREATE TABLE elder_disease (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  elder_id BIGINT NOT NULL COMMENT '老人ID',
  disease_id BIGINT NOT NULL COMMENT '疾病ID',
  diagnosed_date DATE DEFAULT NULL COMMENT '确诊日期',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_elder_disease (org_id, elder_id, disease_id),
  KEY idx_elder_disease_elder_id (elder_id),
  KEY idx_elder_disease_disease_id (disease_id)
) COMMENT='老人疾病';

-- 商品标签
CREATE TABLE product_tag (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  tag_code VARCHAR(64) NOT NULL COMMENT '标签编码',
  tag_name VARCHAR(128) NOT NULL COMMENT '标签名称',
  tag_type VARCHAR(32) DEFAULT NULL COMMENT '标签类型',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_product_tag_org_code (org_id, tag_code),
  KEY idx_product_tag_name (tag_name)
) COMMENT='商品标签';

-- 疾病禁忌标签
CREATE TABLE disease_forbidden_tag (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  disease_id BIGINT NOT NULL COMMENT '疾病ID',
  tag_id BIGINT NOT NULL COMMENT '商品标签ID',
  forbidden_level TINYINT NOT NULL DEFAULT 1 COMMENT '禁忌级别 1禁止 2提示',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_disease_tag (org_id, disease_id, tag_id),
  KEY idx_disease_tag_disease_id (disease_id),
  KEY idx_disease_tag_tag_id (tag_id)
) COMMENT='疾病禁忌标签';

-- 疾病禁忌标签审计日志
CREATE TABLE disease_forbidden_tag_audit (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  disease_id BIGINT NOT NULL COMMENT '疾病ID',
  action_type VARCHAR(32) NOT NULL COMMENT '动作类型 UPDATE',
  before_tag_ids VARCHAR(512) DEFAULT NULL COMMENT '变更前TagIds',
  after_tag_ids VARCHAR(512) DEFAULT NULL COMMENT '变更后TagIds',
  operator_staff_id BIGINT DEFAULT NULL COMMENT '操作员工ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_disease_forbidden_audit_org (org_id),
  KEY idx_disease_forbidden_audit_disease (disease_id),
  KEY idx_disease_forbidden_audit_time (create_time)
) COMMENT='疾病禁忌标签审计';

-- 商品
CREATE TABLE product (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  product_code VARCHAR(64) NOT NULL COMMENT '商品编码',
  product_name VARCHAR(128) NOT NULL COMMENT '商品名称',
  category VARCHAR(64) DEFAULT NULL COMMENT '分类',
  unit VARCHAR(16) DEFAULT NULL COMMENT '单位',
  price DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '售价',
  points_price INT NOT NULL DEFAULT 0 COMMENT '积分价',
  safety_stock INT NOT NULL DEFAULT 0 COMMENT '安全库存',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1上架 0下架',
  tag_ids VARCHAR(255) DEFAULT NULL COMMENT '标签ID集合(逗号分隔)',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_product_org_code (org_id, product_code),
  KEY idx_product_name (product_name)
) COMMENT='商品';

-- 库存批次
CREATE TABLE inventory_batch (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  product_id BIGINT NOT NULL COMMENT '商品ID',
  batch_no VARCHAR(64) NOT NULL COMMENT '批次号',
  quantity INT NOT NULL DEFAULT 0 COMMENT '数量',
  cost_price DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '成本价',
  expire_date DATE DEFAULT NULL COMMENT '有效期',
  warehouse_location VARCHAR(64) DEFAULT NULL COMMENT '库位',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_inventory_batch (org_id, product_id, batch_no),
  KEY idx_inventory_batch_product_id (product_id),
  KEY idx_inventory_batch_expire_date (expire_date)
) COMMENT='库存批次';

-- 库存流水
CREATE TABLE inventory_log (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  product_id BIGINT NOT NULL COMMENT '商品ID',
  batch_id BIGINT DEFAULT NULL COMMENT '批次ID',
  change_type VARCHAR(32) NOT NULL COMMENT '变更类型 IN/OUT/ADJUST',
  change_qty INT NOT NULL COMMENT '变更数量',
  ref_order_id BIGINT DEFAULT NULL COMMENT '关联订单ID',
  ref_adjustment_id BIGINT DEFAULT NULL COMMENT '关联盘点ID',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_inventory_log_product_id (product_id),
  KEY idx_inventory_log_batch_id (batch_id),
  KEY idx_inventory_log_ref_order_id (ref_order_id)
) COMMENT='库存流水';

-- 库存盘点/调整
CREATE TABLE inventory_adjustment (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  product_id BIGINT NOT NULL COMMENT '商品ID',
  batch_id BIGINT DEFAULT NULL COMMENT '批次ID',
  adjust_type VARCHAR(32) NOT NULL COMMENT '盘点类型 GAIN/LOSS',
  adjust_qty INT NOT NULL COMMENT '调整数量',
  reason VARCHAR(255) DEFAULT NULL COMMENT '原因',
  operator_staff_id BIGINT DEFAULT NULL COMMENT '操作员工ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_inventory_adjustment_product_id (product_id),
  KEY idx_inventory_adjustment_batch_id (batch_id)
) COMMENT='库存盘点调整';

-- 订单
CREATE TABLE `order` (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  order_no VARCHAR(64) NOT NULL COMMENT '订单号',
  elder_id BIGINT NOT NULL COMMENT '老人ID',
  total_amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '总金额',
  points_used INT NOT NULL DEFAULT 0 COMMENT '使用积分',
  payable_amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '应付金额',
  pay_status TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态 0未支付 1已支付 2已退款',
  pay_time DATETIME DEFAULT NULL COMMENT '支付时间',
  order_status TINYINT NOT NULL DEFAULT 1 COMMENT '订单状态 1正常 2取消 3完成',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_order_org_no (org_id, order_no),
  KEY idx_order_elder_id (elder_id),
  KEY idx_order_pay_time (pay_time)
) COMMENT='订单';

-- 订单明细
CREATE TABLE order_item (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  order_id BIGINT NOT NULL COMMENT '订单ID',
  product_id BIGINT NOT NULL COMMENT '商品ID',
  product_name VARCHAR(128) NOT NULL COMMENT '商品名称',
  unit_price DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '单价',
  quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
  total_amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '小计',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_order_item_order_id (order_id),
  KEY idx_order_item_product_id (product_id)
) COMMENT='订单明细';

-- 老人积分账户
CREATE TABLE elder_points_account (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  elder_id BIGINT NOT NULL COMMENT '老人ID',
  points_balance INT NOT NULL DEFAULT 0 COMMENT '积分余额',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1正常 0冻结',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_points_account_elder (org_id, elder_id)
) COMMENT='老人积分账户';

-- 积分流水
CREATE TABLE points_log (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  elder_id BIGINT NOT NULL COMMENT '老人ID',
  change_type VARCHAR(32) NOT NULL COMMENT '变更类型 RECHARGE/DEDUCT/ADJUST',
  change_points INT NOT NULL COMMENT '变更积分',
  balance_after INT NOT NULL COMMENT '变更后余额',
  ref_order_id BIGINT DEFAULT NULL COMMENT '关联订单ID',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_points_log_elder_id (elder_id),
  KEY idx_points_log_ref_order_id (ref_order_id)
) COMMENT='积分流水';

-- 探视预约
CREATE TABLE visit_booking (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  elder_id BIGINT NOT NULL COMMENT '老人ID',
  family_user_id BIGINT NOT NULL COMMENT '家属ID',
  visit_date DATE NOT NULL COMMENT '探视日期',
  visit_time DATETIME NOT NULL COMMENT '探视时间',
  visit_time_slot VARCHAR(64) NOT NULL COMMENT '探视时段',
  visitor_count INT NOT NULL DEFAULT 1 COMMENT '来访人数',
  car_plate VARCHAR(32) DEFAULT NULL COMMENT '车牌号',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0待审核 1通过 2拒绝 3取消 4已完成',
  verify_code VARCHAR(64) NOT NULL COMMENT '核验码',
  visit_code VARCHAR(64) NOT NULL COMMENT '探视码',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_visit_booking_elder_id (elder_id),
  KEY idx_visit_booking_family_id (family_user_id),
  KEY idx_visit_booking_visit_date (visit_date),
  KEY idx_visit_booking_visit_time (visit_time),
  UNIQUE KEY uk_visit_booking_code (org_id, visit_code)
) COMMENT='探视预约';

-- 探视核验日志
CREATE TABLE visit_qrcode_check_log (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  booking_id BIGINT NOT NULL COMMENT '预约ID',
  check_time DATETIME NOT NULL COMMENT '核验时间',
  staff_id BIGINT NOT NULL COMMENT '核验员工ID',
  qrcode_token VARCHAR(128) NOT NULL COMMENT '核验二维码Token',
  result_status TINYINT NOT NULL DEFAULT 1 COMMENT '结果 1通过 0拒绝',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_visit_check_booking_id (booking_id),
  KEY idx_visit_check_time (check_time)
) COMMENT='探视核验日志';

-- 月账单
CREATE TABLE bill_monthly (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  elder_id BIGINT NOT NULL COMMENT '老人ID',
  bill_month CHAR(7) NOT NULL COMMENT '账单月份 YYYY-MM',
  total_amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '总金额',
  paid_amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '已支付金额',
  outstanding_amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '未支付金额',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0未结算 1已结算 2已支付',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_bill_monthly (org_id, elder_id, bill_month),
  KEY idx_bill_monthly_elder_id (elder_id),
  KEY idx_bill_monthly_month (bill_month)
) COMMENT='月账单';

-- 账单支付记录
CREATE TABLE payment_record (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  bill_monthly_id BIGINT NOT NULL COMMENT '账单ID',
  amount DECIMAL(10,2) NOT NULL COMMENT '支付金额',
  pay_method VARCHAR(32) NOT NULL COMMENT '支付方式 CASH/BANK/WECHAT_OFFLINE',
  external_txn_id VARCHAR(64) DEFAULT NULL COMMENT '外部流水号',
  paid_at DATETIME NOT NULL COMMENT '支付时间',
  operator_staff_id BIGINT DEFAULT NULL COMMENT '收款员工ID',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_payment_external (org_id, external_txn_id),
  KEY idx_payment_bill (bill_monthly_id),
  KEY idx_payment_paid_at (paid_at)
) COMMENT='账单支付记录';

-- 每日对账
CREATE TABLE reconciliation_daily (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  reconcile_date DATE NOT NULL COMMENT '对账日期',
  total_received DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '当日收款总额',
  mismatch_flag TINYINT NOT NULL DEFAULT 0 COMMENT '是否对账不一致',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_reconcile_date (org_id, reconcile_date)
) COMMENT='每日对账';

-- 月账单明细
CREATE TABLE bill_item (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  bill_monthly_id BIGINT NOT NULL COMMENT '月账单ID',
  item_type VARCHAR(32) NOT NULL COMMENT '项目类型 NURSING/BED/MEAL/SHOP',
  item_name VARCHAR(128) NOT NULL COMMENT '项目名称',
  amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '金额',
  ref_order_id BIGINT DEFAULT NULL COMMENT '关联订单ID',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_bill_item_bill_monthly_id (bill_monthly_id),
  KEY idx_bill_item_ref_order_id (ref_order_id)
) COMMENT='月账单明细';

SET FOREIGN_KEY_CHECKS = 1;

-- 计费配置（按月生效）
CREATE TABLE billing_config (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  config_key VARCHAR(64) NOT NULL COMMENT '配置项键',
  config_value DECIMAL(10,2) NOT NULL COMMENT '配置值',
  effective_month CHAR(7) NOT NULL COMMENT '生效月份 YYYY-MM',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_billing_config (org_id, config_key, effective_month),
  KEY idx_billing_config_org (org_id),
  KEY idx_billing_config_key (config_key),
  KEY idx_billing_config_month (effective_month)
) COMMENT='计费配置';

-- 护理等级计费
CREATE TABLE billing_care_level_fee (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  care_level VARCHAR(32) NOT NULL COMMENT '护理等级',
  fee_monthly DECIMAL(10,2) NOT NULL COMMENT '月护理费',
  effective_month CHAR(7) NOT NULL COMMENT '生效月份 YYYY-MM',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_billing_care_level (org_id, care_level, effective_month),
  KEY idx_billing_care_org (org_id),
  KEY idx_billing_care_level (care_level),
  KEY idx_billing_care_month (effective_month)
) COMMENT='护理等级计费';
