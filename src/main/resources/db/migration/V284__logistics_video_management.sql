CREATE TABLE logistics_camera (
  id BIGINT NOT NULL PRIMARY KEY,
  org_id BIGINT NOT NULL,
  camera_code VARCHAR(64) NOT NULL,
  camera_name VARCHAR(128) NOT NULL,
  location VARCHAR(128) NOT NULL,
  channel_ref VARCHAR(128) NOT NULL COMMENT '采集网关通道标识，不保存含密码的流地址',
  protocol VARCHAR(16) NOT NULL,
  responsible_name VARCHAR(64) DEFAULT NULL,
  enabled TINYINT NOT NULL DEFAULT 1,
  bitrate_mbps DECIMAL(8,2) NOT NULL DEFAULT 2,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_camera_org_code (org_id, camera_code),
  KEY idx_camera_org (org_id, enabled)
) COMMENT='后勤摄像头档案';

CREATE TABLE logistics_video_policy (
  id BIGINT NOT NULL PRIMARY KEY,
  org_id BIGINT NOT NULL,
  provider VARCHAR(16) NOT NULL DEFAULT 'UNDECIDED',
  retention_days INT NOT NULL DEFAULT 90,
  auto_cleanup TINYINT NOT NULL DEFAULT 0,
  basis VARCHAR(255) DEFAULT NULL COMMENT '留存依据/机构制度',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_video_policy_org (org_id)
) COMMENT='录像存储与留存策略';

CREATE TABLE logistics_video_recording (
  id BIGINT NOT NULL PRIMARY KEY,
  org_id BIGINT NOT NULL,
  camera_id BIGINT NOT NULL,
  ingest_key VARCHAR(64) NOT NULL,
  provider VARCHAR(16) NOT NULL,
  object_key VARCHAR(255) NOT NULL,
  started_at DATETIME NOT NULL,
  ended_at DATETIME NOT NULL,
  expires_at DATETIME NOT NULL,
  size_bytes BIGINT NOT NULL,
  sha256 VARCHAR(64) NOT NULL,
  held TINYINT NOT NULL DEFAULT 0,
  hold_reason VARCHAR(255) DEFAULT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'AVAILABLE',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_video_ingest (org_id, camera_id, ingest_key),
  KEY idx_video_camera_time (org_id, camera_id, started_at),
  KEY idx_video_expiry (org_id, status, held, expires_at)
) COMMENT='私有录像分片索引及保全状态';
