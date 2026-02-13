-- unify store & inventory snapshots
ALTER TABLE inventory_log
  ADD COLUMN product_code_snapshot VARCHAR(64) DEFAULT NULL COMMENT '商品编码快照',
  ADD COLUMN product_name_snapshot VARCHAR(128) DEFAULT NULL COMMENT '商品名称快照',
  ADD COLUMN biz_type VARCHAR(32) DEFAULT NULL COMMENT '业务类型 INBOUND/ORDER/CONSUME/RETURN/ADJUST';

ALTER TABLE order_item
  ADD COLUMN product_code_snapshot VARCHAR(64) DEFAULT NULL COMMENT '商品编码快照',
  ADD COLUMN product_name_snapshot VARCHAR(128) DEFAULT NULL COMMENT '商品名称快照';

-- backfill snapshots for existing data
UPDATE inventory_log l
JOIN product p ON l.product_id = p.id
SET l.product_code_snapshot = p.product_code,
    l.product_name_snapshot = p.product_name
WHERE l.product_name_snapshot IS NULL;

UPDATE order_item oi
JOIN product p ON oi.product_id = p.id
SET oi.product_code_snapshot = p.product_code,
    oi.product_name_snapshot = p.product_name
WHERE oi.product_name_snapshot IS NULL;
