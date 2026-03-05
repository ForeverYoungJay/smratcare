ALTER TABLE shift_handover
  ADD COLUMN attachment_urls VARCHAR(2000) DEFAULT NULL COMMENT '交接附件(JSON)';
