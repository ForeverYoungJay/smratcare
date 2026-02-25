CREATE TABLE IF NOT EXISTS base_data_item (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  config_group VARCHAR(64) NOT NULL COMMENT '配置分组',
  item_code VARCHAR(64) NOT NULL COMMENT '配置编码',
  item_name VARCHAR(128) NOT NULL COMMENT '配置名称',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0停用 1启用',
  sort_no INT DEFAULT 0 COMMENT '排序',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT DEFAULT 0,
  UNIQUE KEY uk_base_data_item_tenant_group_code (tenant_id, config_group, item_code),
  UNIQUE KEY uk_base_data_item_tenant_group_name (tenant_id, config_group, item_name),
  KEY idx_base_data_item_tenant_group (tenant_id, config_group),
  KEY idx_base_data_item_org_status (org_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='基础数据配置项';
