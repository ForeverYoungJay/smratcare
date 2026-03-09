CREATE TABLE IF NOT EXISTS family_portal_state (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  family_user_id BIGINT NOT NULL COMMENT '家属用户ID',
  category VARCHAR(32) NOT NULL COMMENT '状态分类',
  biz_key VARCHAR(64) NOT NULL DEFAULT '' COMMENT '业务键',
  value_json LONGTEXT NOT NULL COMMENT 'JSON值',
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
  created_by BIGINT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_family_portal_state_unique (org_id, family_user_id, category, biz_key),
  KEY idx_family_portal_state_category_time (org_id, family_user_id, category, update_time),
  KEY idx_family_portal_state_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='家属端聚合状态存储';
