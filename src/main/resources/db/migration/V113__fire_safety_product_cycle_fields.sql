ALTER TABLE fire_safety_record
  ADD COLUMN product_production_date DATE DEFAULT NULL COMMENT '产品生产日期' AFTER equipment_batch_no,
  ADD COLUMN product_expiry_date DATE DEFAULT NULL COMMENT '产品过期日期' AFTER product_production_date,
  ADD COLUMN check_cycle_days INT DEFAULT NULL COMMENT '检查周期(天)' AFTER product_expiry_date;

ALTER TABLE fire_safety_record
  ADD KEY idx_fire_record_org_product_expiry (org_id, product_expiry_date);
