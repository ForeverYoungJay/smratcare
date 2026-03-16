ALTER TABLE fire_safety_record
  ADD COLUMN image_urls TEXT NULL COMMENT '现场图片地址，按换行分隔' AFTER equipment_aging_disposal;
