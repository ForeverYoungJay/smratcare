-- 员工移动端交接与巡检扫码留痕

CREATE TABLE IF NOT EXISTS operations_staff_handover (
  id BIGINT PRIMARY KEY COMMENT '主键',
  tenant_id BIGINT DEFAULT NULL COMMENT '租户ID',
  org_id BIGINT DEFAULT NULL COMMENT '机构ID',
  staff_id BIGINT DEFAULT NULL COMMENT '员工ID',
  title VARCHAR(128) NOT NULL COMMENT '交接标题',
  owner VARCHAR(64) DEFAULT NULL COMMENT '交接人',
  content TEXT COMMENT '交接内容',
  handover_time DATETIME NOT NULL COMMENT '交接时间',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT DEFAULT 0 COMMENT '是否删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工移动端交接记录';

CREATE INDEX idx_staff_handover_org_time ON operations_staff_handover (org_id, handover_time);
CREATE INDEX idx_staff_handover_staff_time ON operations_staff_handover (staff_id, handover_time);

CREATE TABLE IF NOT EXISTS operations_staff_patrol_scan (
  id BIGINT PRIMARY KEY COMMENT '主键',
  tenant_id BIGINT DEFAULT NULL COMMENT '租户ID',
  org_id BIGINT DEFAULT NULL COMMENT '机构ID',
  staff_id BIGINT DEFAULT NULL COMMENT '员工ID',
  point_id VARCHAR(64) NOT NULL COMMENT '巡检点编号',
  point_name VARCHAR(128) DEFAULT NULL COMMENT '巡检点名称',
  location VARCHAR(128) DEFAULT NULL COMMENT '巡检位置',
  scan_text VARCHAR(255) DEFAULT NULL COMMENT '扫码原文',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  scan_time DATETIME NOT NULL COMMENT '扫码时间',
  status VARCHAR(32) DEFAULT 'DONE' COMMENT '状态',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT DEFAULT 0 COMMENT '是否删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工移动端巡检扫码记录';

CREATE INDEX idx_staff_patrol_org_time ON operations_staff_patrol_scan (org_id, scan_time);
CREATE INDEX idx_staff_patrol_point_time ON operations_staff_patrol_scan (point_id, scan_time);
