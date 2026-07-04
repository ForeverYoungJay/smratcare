CREATE TABLE IF NOT EXISTS operations_staff_handover (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT,
  org_id BIGINT,
  staff_id BIGINT,
  title VARCHAR(128) NOT NULL,
  owner VARCHAR(64),
  content CLOB,
  handover_time TIMESTAMP NOT NULL,
  created_by BIGINT,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_staff_handover_org_time ON operations_staff_handover (org_id, handover_time);
CREATE INDEX IF NOT EXISTS idx_staff_handover_staff_time ON operations_staff_handover (staff_id, handover_time);

CREATE TABLE IF NOT EXISTS operations_staff_patrol_scan (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT,
  org_id BIGINT,
  staff_id BIGINT,
  point_id VARCHAR(64) NOT NULL,
  point_name VARCHAR(128),
  location VARCHAR(128),
  scan_text VARCHAR(255),
  remark VARCHAR(255),
  scan_time TIMESTAMP NOT NULL,
  status VARCHAR(32) DEFAULT 'DONE',
  created_by BIGINT,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_staff_patrol_org_time ON operations_staff_patrol_scan (org_id, scan_time);
CREATE INDEX IF NOT EXISTS idx_staff_patrol_point_time ON operations_staff_patrol_scan (point_id, scan_time);
