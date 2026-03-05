CREATE TABLE IF NOT EXISTS oa_document_folder (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  name VARCHAR(128) NOT NULL COMMENT '档案夹名称',
  parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '上级档案夹ID，0表示根',
  sort_no INT NOT NULL DEFAULT 0 COMMENT '排序',
  status VARCHAR(16) NOT NULL DEFAULT 'ENABLED' COMMENT '状态 ENABLED/DISABLED',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_oa_doc_folder_org_parent (org_id, parent_id),
  KEY idx_oa_doc_folder_org_status (org_id, status)
) COMMENT='OA文档档案夹';

SET @has_folder_id := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'oa_document'
    AND COLUMN_NAME = 'folder_id'
);
SET @ddl_add_folder_id := IF(
  @has_folder_id = 0,
  'ALTER TABLE oa_document ADD COLUMN folder_id BIGINT DEFAULT NULL COMMENT ''档案夹ID'' AFTER folder',
  'SELECT 1'
);
PREPARE stmt_add_folder_id FROM @ddl_add_folder_id;
EXECUTE stmt_add_folder_id;
DEALLOCATE PREPARE stmt_add_folder_id;

SET @has_folder_id_index := (
  SELECT COUNT(1)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'oa_document'
    AND INDEX_NAME = 'idx_document_org_folder_id'
);
SET @ddl_add_folder_id_index := IF(
  @has_folder_id_index = 0,
  'ALTER TABLE oa_document ADD KEY idx_document_org_folder_id (org_id, folder_id)',
  'SELECT 1'
);
PREPARE stmt_add_folder_id_index FROM @ddl_add_folder_id_index;
EXECUTE stmt_add_folder_id_index;
DEALLOCATE PREPARE stmt_add_folder_id_index;
