-- 电子文件存档：按分类归档长者的合同/医疗/证件/其他文件

CREATE TABLE IF NOT EXISTS elder_file_archive (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NULL,
  org_id BIGINT NOT NULL,
  elder_id BIGINT NOT NULL,
  category VARCHAR(24) NOT NULL COMMENT 'CONTRACT 合同 / MEDICAL 医疗 / CERTIFICATE 证件 / OTHER 其他',
  file_name VARCHAR(200) NOT NULL,
  file_url VARCHAR(512) NOT NULL,
  file_size BIGINT NULL,
  remark VARCHAR(200) NULL,
  uploaded_by BIGINT NULL,
  uploaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_elder_file_archive_elder (org_id, elder_id, category, is_deleted)
) COMMENT='长者电子文件存档';
