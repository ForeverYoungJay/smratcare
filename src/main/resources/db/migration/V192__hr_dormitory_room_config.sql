CREATE TABLE IF NOT EXISTS hr_dormitory_room_config (
  id BIGINT NOT NULL,
  org_id BIGINT NOT NULL,
  building VARCHAR(64) NOT NULL,
  floor_label VARCHAR(64) DEFAULT NULL,
  room_no VARCHAR(64) NOT NULL,
  bed_capacity INT NOT NULL DEFAULT 0,
  status VARCHAR(16) NOT NULL DEFAULT 'ENABLED',
  sort_no INT NOT NULL DEFAULT 0,
  remark VARCHAR(255) DEFAULT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_hr_dormitory_room_config_org_room (org_id, building, room_no, is_deleted),
  KEY idx_hr_dormitory_room_config_org_building (org_id, building, is_deleted),
  KEY idx_hr_dormitory_room_config_org_status (org_id, status, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工宿舍房间基础配置';
