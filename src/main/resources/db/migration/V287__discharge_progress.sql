-- 退住办理分步进度：护理/医务交接、物品及房间交接、档案归档（费用结清与床位释放沿用结算单与 auto_discharge_status）
ALTER TABLE elder_discharge_apply
  ADD COLUMN care_handover_time DATETIME DEFAULT NULL COMMENT '护理/医务交接完成时间',
  ADD COLUMN care_handover_by_name VARCHAR(64) DEFAULT NULL COMMENT '护理/医务交接确认人',
  ADD COLUMN item_handover_time DATETIME DEFAULT NULL COMMENT '物品及房间交接完成时间',
  ADD COLUMN item_handover_by_name VARCHAR(64) DEFAULT NULL COMMENT '物品及房间交接确认人',
  ADD COLUMN archive_time DATETIME DEFAULT NULL COMMENT '档案归档时间',
  ADD COLUMN archive_by_name VARCHAR(64) DEFAULT NULL COMMENT '档案归档确认人';
