-- 领用出库：领用单（多物品）+ 领用单号

SET @has_outbound_no := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'inventory_log'
    AND COLUMN_NAME = 'outbound_no'
);
SET @sql_outbound_no := IF(
  @has_outbound_no = 0,
  'ALTER TABLE inventory_log ADD COLUMN outbound_no VARCHAR(64) DEFAULT NULL COMMENT ''领用单号'' AFTER receiver_name',
  'SELECT 1'
);
PREPARE stmt FROM @sql_outbound_no;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_outbound_no := (
  SELECT COUNT(1)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'inventory_log'
    AND INDEX_NAME = 'idx_inventory_log_outbound_no'
);
SET @sql_idx_outbound_no := IF(
  @idx_outbound_no = 0,
  'CREATE INDEX idx_inventory_log_outbound_no ON inventory_log (outbound_no)',
  'SELECT 1'
);
PREPARE stmt FROM @sql_idx_outbound_no;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS inventory_outbound_sheet (
  id BIGINT PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  outbound_no VARCHAR(64) NOT NULL COMMENT '领用单号',
  receiver_name VARCHAR(64) NOT NULL COMMENT '领取人',
  elder_id BIGINT DEFAULT NULL COMMENT '老人院内ID',
  contract_no VARCHAR(64) DEFAULT NULL COMMENT '合同号',
  apply_dept VARCHAR(64) DEFAULT NULL COMMENT '申请部门',
  operator_staff_id BIGINT DEFAULT NULL COMMENT '经办人ID',
  operator_name VARCHAR(64) DEFAULT NULL COMMENT '经办人',
  status VARCHAR(16) NOT NULL DEFAULT 'DRAFT' COMMENT '状态 DRAFT/CONFIRMED',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  confirm_staff_id BIGINT DEFAULT NULL COMMENT '确认人',
  confirm_time DATETIME DEFAULT NULL COMMENT '确认时间',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT(1) DEFAULT 0 COMMENT '删除标记'
) COMMENT='领用出库单';

SET @has_sheet_elder_id := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'inventory_outbound_sheet'
    AND COLUMN_NAME = 'elder_id'
);
SET @sql_sheet_elder_id := IF(
  @has_sheet_elder_id = 0,
  'ALTER TABLE inventory_outbound_sheet ADD COLUMN elder_id BIGINT DEFAULT NULL COMMENT ''老人院内ID'' AFTER receiver_name',
  'SELECT 1'
);
PREPARE stmt FROM @sql_sheet_elder_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_sheet_contract_no := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'inventory_outbound_sheet'
    AND COLUMN_NAME = 'contract_no'
);
SET @sql_sheet_contract_no := IF(
  @has_sheet_contract_no = 0,
  'ALTER TABLE inventory_outbound_sheet ADD COLUMN contract_no VARCHAR(64) DEFAULT NULL COMMENT ''合同号'' AFTER elder_id',
  'SELECT 1'
);
PREPARE stmt FROM @sql_sheet_contract_no;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_sheet_apply_dept := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'inventory_outbound_sheet'
    AND COLUMN_NAME = 'apply_dept'
);
SET @sql_sheet_apply_dept := IF(
  @has_sheet_apply_dept = 0,
  'ALTER TABLE inventory_outbound_sheet ADD COLUMN apply_dept VARCHAR(64) DEFAULT NULL COMMENT ''申请部门'' AFTER contract_no',
  'SELECT 1'
);
PREPARE stmt FROM @sql_sheet_apply_dept;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_sheet_operator_staff_id := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'inventory_outbound_sheet'
    AND COLUMN_NAME = 'operator_staff_id'
);
SET @sql_sheet_operator_staff_id := IF(
  @has_sheet_operator_staff_id = 0,
  'ALTER TABLE inventory_outbound_sheet ADD COLUMN operator_staff_id BIGINT DEFAULT NULL COMMENT ''经办人ID'' AFTER apply_dept',
  'SELECT 1'
);
PREPARE stmt FROM @sql_sheet_operator_staff_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_sheet_operator_name := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'inventory_outbound_sheet'
    AND COLUMN_NAME = 'operator_name'
);
SET @sql_sheet_operator_name := IF(
  @has_sheet_operator_name = 0,
  'ALTER TABLE inventory_outbound_sheet ADD COLUMN operator_name VARCHAR(64) DEFAULT NULL COMMENT ''经办人'' AFTER operator_staff_id',
  'SELECT 1'
);
PREPARE stmt FROM @sql_sheet_operator_name;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_sheet_no := (
  SELECT COUNT(1)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'inventory_outbound_sheet'
    AND INDEX_NAME = 'uk_inventory_outbound_sheet_no'
);
SET @sql_idx_sheet_no := IF(
  @idx_sheet_no = 0,
  'CREATE UNIQUE INDEX uk_inventory_outbound_sheet_no ON inventory_outbound_sheet (org_id, outbound_no)',
  'SELECT 1'
);
PREPARE stmt FROM @sql_idx_sheet_no;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_sheet_status := (
  SELECT COUNT(1)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'inventory_outbound_sheet'
    AND INDEX_NAME = 'idx_inventory_outbound_sheet_status'
);
SET @sql_idx_sheet_status := IF(
  @idx_sheet_status = 0,
  'CREATE INDEX idx_inventory_outbound_sheet_status ON inventory_outbound_sheet (org_id, status, create_time)',
  'SELECT 1'
);
PREPARE stmt FROM @sql_idx_sheet_status;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS inventory_outbound_sheet_item (
  id BIGINT PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  sheet_id BIGINT NOT NULL COMMENT '领用单ID',
  product_id BIGINT NOT NULL COMMENT '商品ID',
  warehouse_id BIGINT DEFAULT NULL COMMENT '仓库ID',
  batch_id BIGINT DEFAULT NULL COMMENT '批次ID',
  quantity INT NOT NULL COMMENT '数量',
  reason VARCHAR(255) DEFAULT NULL COMMENT '领用原因',
  item_sort INT DEFAULT 0 COMMENT '排序',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT(1) DEFAULT 0 COMMENT '删除标记'
) COMMENT='领用出库单明细';

SET @idx_item_sheet := (
  SELECT COUNT(1)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'inventory_outbound_sheet_item'
    AND INDEX_NAME = 'idx_inventory_outbound_item_sheet'
);
SET @sql_idx_item_sheet := IF(
  @idx_item_sheet = 0,
  'CREATE INDEX idx_inventory_outbound_item_sheet ON inventory_outbound_sheet_item (org_id, sheet_id, item_sort)',
  'SELECT 1'
);
PREPARE stmt FROM @sql_idx_item_sheet;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_item_product := (
  SELECT COUNT(1)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'inventory_outbound_sheet_item'
    AND INDEX_NAME = 'idx_inventory_outbound_item_product'
);
SET @sql_idx_item_product := IF(
  @idx_item_product = 0,
  'CREATE INDEX idx_inventory_outbound_item_product ON inventory_outbound_sheet_item (org_id, product_id)',
  'SELECT 1'
);
PREPARE stmt FROM @sql_idx_item_product;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
