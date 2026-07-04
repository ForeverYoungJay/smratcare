-- 员工移动端异常上报补齐位置、扫码与附件证据

ALTER TABLE incident_report
  ADD COLUMN location VARCHAR(128) DEFAULT NULL COMMENT '异常位置' AFTER level,
  ADD COLUMN scan_text VARCHAR(255) DEFAULT NULL COMMENT '扫码结果' AFTER location,
  ADD COLUMN attachment_urls TEXT COMMENT '附件URL，逗号分隔' AFTER scan_text;
