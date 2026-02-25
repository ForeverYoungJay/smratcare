-- M9 第二批：库存按仓维度与执行联动增强

ALTER TABLE inventory_batch
  ADD COLUMN warehouse_id BIGINT DEFAULT NULL COMMENT '仓库ID' AFTER product_id,
  ADD KEY idx_inventory_batch_warehouse_id (warehouse_id);

ALTER TABLE inventory_log
  ADD COLUMN warehouse_id BIGINT DEFAULT NULL COMMENT '仓库ID' AFTER batch_id,
  ADD KEY idx_inventory_log_warehouse_id (warehouse_id);

ALTER TABLE inventory_batch
  DROP INDEX uk_inventory_batch,
  ADD UNIQUE KEY uk_inventory_batch (org_id, product_id, batch_no, warehouse_id);
