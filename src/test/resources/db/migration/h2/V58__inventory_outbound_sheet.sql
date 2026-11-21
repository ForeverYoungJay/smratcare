-- 测试库(H2)补齐领用单表，与正式库一致（除主键外均可为空）

CREATE TABLE IF NOT EXISTS inventory_outbound_sheet (id BIGINT NOT NULL PRIMARY KEY);
ALTER TABLE inventory_outbound_sheet ADD COLUMN IF NOT EXISTS org_id BIGINT;
ALTER TABLE inventory_outbound_sheet ADD COLUMN IF NOT EXISTS outbound_no VARCHAR(64);
ALTER TABLE inventory_outbound_sheet ADD COLUMN IF NOT EXISTS receiver_name VARCHAR(64);
ALTER TABLE inventory_outbound_sheet ADD COLUMN IF NOT EXISTS elder_id BIGINT;
ALTER TABLE inventory_outbound_sheet ADD COLUMN IF NOT EXISTS contract_no VARCHAR(64);
ALTER TABLE inventory_outbound_sheet ADD COLUMN IF NOT EXISTS apply_dept VARCHAR(64);
ALTER TABLE inventory_outbound_sheet ADD COLUMN IF NOT EXISTS operator_staff_id BIGINT;
ALTER TABLE inventory_outbound_sheet ADD COLUMN IF NOT EXISTS operator_name VARCHAR(64);
ALTER TABLE inventory_outbound_sheet ADD COLUMN IF NOT EXISTS status VARCHAR(16);
ALTER TABLE inventory_outbound_sheet ADD COLUMN IF NOT EXISTS remark VARCHAR(255);
ALTER TABLE inventory_outbound_sheet ADD COLUMN IF NOT EXISTS confirm_staff_id BIGINT;
ALTER TABLE inventory_outbound_sheet ADD COLUMN IF NOT EXISTS confirm_time DATETIME;
ALTER TABLE inventory_outbound_sheet ADD COLUMN IF NOT EXISTS create_time DATETIME DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE inventory_outbound_sheet ADD COLUMN IF NOT EXISTS update_time DATETIME DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE inventory_outbound_sheet ADD COLUMN IF NOT EXISTS is_deleted TINYINT DEFAULT 0;

CREATE TABLE IF NOT EXISTS inventory_outbound_sheet_item (id BIGINT NOT NULL PRIMARY KEY);
ALTER TABLE inventory_outbound_sheet_item ADD COLUMN IF NOT EXISTS org_id BIGINT;
ALTER TABLE inventory_outbound_sheet_item ADD COLUMN IF NOT EXISTS sheet_id BIGINT;
ALTER TABLE inventory_outbound_sheet_item ADD COLUMN IF NOT EXISTS product_id BIGINT;
ALTER TABLE inventory_outbound_sheet_item ADD COLUMN IF NOT EXISTS warehouse_id BIGINT;
ALTER TABLE inventory_outbound_sheet_item ADD COLUMN IF NOT EXISTS batch_id BIGINT;
ALTER TABLE inventory_outbound_sheet_item ADD COLUMN IF NOT EXISTS quantity INT;
ALTER TABLE inventory_outbound_sheet_item ADD COLUMN IF NOT EXISTS reason VARCHAR(255);
ALTER TABLE inventory_outbound_sheet_item ADD COLUMN IF NOT EXISTS item_sort INT;
ALTER TABLE inventory_outbound_sheet_item ADD COLUMN IF NOT EXISTS create_time DATETIME DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE inventory_outbound_sheet_item ADD COLUMN IF NOT EXISTS update_time DATETIME DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE inventory_outbound_sheet_item ADD COLUMN IF NOT EXISTS is_deleted TINYINT DEFAULT 0;
