ALTER TABLE fire_safety_record
  ADD COLUMN qr_token VARCHAR(64) DEFAULT NULL COMMENT '巡查二维码令牌' AFTER next_check_date,
  ADD COLUMN qr_generated_at DATETIME DEFAULT NULL COMMENT '二维码生成时间' AFTER qr_token,
  ADD COLUMN scan_completed_at DATETIME DEFAULT NULL COMMENT '扫码完成时间' AFTER qr_generated_at,
  ADD COLUMN duty_record VARCHAR(1024) DEFAULT NULL COMMENT '值班记录' AFTER scan_completed_at,
  ADD COLUMN handover_punch_time DATETIME DEFAULT NULL COMMENT '交接班打卡时间' AFTER duty_record,
  ADD COLUMN equipment_batch_no VARCHAR(128) DEFAULT NULL COMMENT '设备批号' AFTER handover_punch_time,
  ADD COLUMN equipment_update_note VARCHAR(1024) DEFAULT NULL COMMENT '设备更新记录' AFTER equipment_batch_no,
  ADD COLUMN equipment_aging_disposal VARCHAR(1024) DEFAULT NULL COMMENT '设备老化处置记录' AFTER equipment_update_note;

ALTER TABLE fire_safety_record
  ADD KEY idx_fire_record_org_qr_token (org_id, qr_token),
  ADD KEY idx_fire_record_org_scan_completed (org_id, scan_completed_at),
  ADD KEY idx_fire_record_org_handover_punch (org_id, handover_punch_time);
