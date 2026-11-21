-- 电子文件存档（主库定义见 V281）

CREATE TABLE IF NOT EXISTS elder_file_archive (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT DEFAULT NULL,
  org_id BIGINT NOT NULL,
  elder_id BIGINT NOT NULL,
  category VARCHAR(24) NOT NULL,
  file_name VARCHAR(200) NOT NULL,
  file_url VARCHAR(512) NOT NULL,
  file_size BIGINT DEFAULT NULL,
  remark VARCHAR(200) DEFAULT NULL,
  uploaded_by BIGINT DEFAULT NULL,
  uploaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_elder_file_archive_elder
  ON elder_file_archive (org_id, elder_id, category, is_deleted);
