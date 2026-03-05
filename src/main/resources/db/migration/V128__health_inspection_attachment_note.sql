ALTER TABLE health_inspection
  ADD COLUMN attachment_urls VARCHAR(2000) DEFAULT NULL COMMENT '巡检附件照片(JSON)' AFTER follow_up_action,
  ADD COLUMN other_note VARCHAR(1000) DEFAULT NULL COMMENT '其他说明' AFTER attachment_urls;
