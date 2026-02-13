-- OA 日程、考勤、报表

CREATE TABLE staff_schedule (
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

CREATE TABLE attendance_record (
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

CREATE TABLE report_record (
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
