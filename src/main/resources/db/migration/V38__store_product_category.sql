CREATE TABLE IF NOT EXISTS product_category (
  id BIGINT NOT NULL COMMENT '主键',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  category_code VARCHAR(64) NOT NULL COMMENT '分类编码',
  category_name VARCHAR(128) NOT NULL COMMENT '分类名称',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_product_category_org_code (org_id, category_code),
  KEY idx_product_category_org_status (org_id, status),
  KEY idx_product_category_org_name (org_id, category_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品大类';
