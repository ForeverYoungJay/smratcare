-- M9 物资管理主数据与采购/调拨单

CREATE TABLE IF NOT EXISTS material_warehouse (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  warehouse_code VARCHAR(64) NOT NULL COMMENT '仓库编码',
  warehouse_name VARCHAR(128) NOT NULL COMMENT '仓库名称',
  manager_name VARCHAR(64) DEFAULT NULL COMMENT '负责人',
  manager_phone VARCHAR(32) DEFAULT NULL COMMENT '联系电话',
  address VARCHAR(255) DEFAULT NULL COMMENT '仓库地址',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_material_warehouse_code (org_id, warehouse_code),
  KEY idx_material_warehouse_name (warehouse_name)
) COMMENT='物资仓库';

CREATE TABLE IF NOT EXISTS material_supplier (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  supplier_code VARCHAR(64) NOT NULL COMMENT '供应商编码',
  supplier_name VARCHAR(128) NOT NULL COMMENT '供应商名称',
  contact_name VARCHAR(64) DEFAULT NULL COMMENT '联系人',
  contact_phone VARCHAR(32) DEFAULT NULL COMMENT '联系电话',
  address VARCHAR(255) DEFAULT NULL COMMENT '地址',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_material_supplier_code (org_id, supplier_code),
  KEY idx_material_supplier_name (supplier_name)
) COMMENT='物资供应商';

CREATE TABLE IF NOT EXISTS material_purchase_order (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  order_no VARCHAR(64) NOT NULL COMMENT '采购单号',
  warehouse_id BIGINT DEFAULT NULL COMMENT '仓库ID',
  supplier_id BIGINT DEFAULT NULL COMMENT '供应商ID',
  order_date DATE NOT NULL COMMENT '下单日期',
  status VARCHAR(32) NOT NULL DEFAULT 'DRAFT' COMMENT '状态 DRAFT/APPROVED/COMPLETED/CANCELLED',
  total_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '总金额',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_material_po_no (org_id, order_no),
  KEY idx_material_po_status (status),
  KEY idx_material_po_order_date (order_date)
) COMMENT='物资采购单';

CREATE TABLE IF NOT EXISTS material_purchase_order_item (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  order_id BIGINT NOT NULL COMMENT '采购单ID',
  product_id BIGINT NOT NULL COMMENT '物资ID(商品ID)',
  product_name_snapshot VARCHAR(128) DEFAULT NULL COMMENT '物资名称快照',
  quantity INT NOT NULL DEFAULT 0 COMMENT '采购数量',
  unit_price DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '采购单价',
  amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '采购金额',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_material_po_item_order (order_id),
  KEY idx_material_po_item_product (product_id)
) COMMENT='物资采购单明细';

CREATE TABLE IF NOT EXISTS material_transfer_order (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  transfer_no VARCHAR(64) NOT NULL COMMENT '调拨单号',
  from_warehouse_id BIGINT NOT NULL COMMENT '调出仓库ID',
  to_warehouse_id BIGINT NOT NULL COMMENT '调入仓库ID',
  status VARCHAR(32) NOT NULL DEFAULT 'DRAFT' COMMENT '状态 DRAFT/COMPLETED/CANCELLED',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_material_transfer_no (org_id, transfer_no),
  KEY idx_material_transfer_status (status)
) COMMENT='物资调拨单';

CREATE TABLE IF NOT EXISTS material_transfer_item (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  transfer_id BIGINT NOT NULL COMMENT '调拨单ID',
  product_id BIGINT NOT NULL COMMENT '物资ID(商品ID)',
  product_name_snapshot VARCHAR(128) DEFAULT NULL COMMENT '物资名称快照',
  quantity INT NOT NULL DEFAULT 0 COMMENT '调拨数量',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_material_transfer_item_transfer (transfer_id),
  KEY idx_material_transfer_item_product (product_id)
) COMMENT='物资调拨单明细';
