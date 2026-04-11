ALTER TABLE bed
  ADD COLUMN occupancy_source VARCHAR(32) DEFAULT NULL COMMENT '占用来源: SELF/RESERVATION/MAINTENANCE/FROZEN/CLEANING' AFTER elder_id,
  ADD COLUMN occupancy_ref_type VARCHAR(32) DEFAULT NULL COMMENT '占用关联类型: ELDER/LEAD/CONTRACT/SYSTEM' AFTER occupancy_source,
  ADD COLUMN occupancy_ref_id BIGINT DEFAULT NULL COMMENT '占用关联ID' AFTER occupancy_ref_type,
  ADD COLUMN lock_expires_at DATETIME DEFAULT NULL COMMENT '锁床失效时间' AFTER occupancy_ref_id,
  ADD COLUMN occupancy_note VARCHAR(255) DEFAULT NULL COMMENT '占用备注' AFTER lock_expires_at,
  ADD COLUMN last_release_reason VARCHAR(255) DEFAULT NULL COMMENT '最近一次释放原因' AFTER occupancy_note,
  ADD COLUMN last_released_at DATETIME DEFAULT NULL COMMENT '最近一次释放时间' AFTER last_release_reason;
