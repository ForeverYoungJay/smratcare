-- 员工移动端任务回执与异常上报补齐语音证据

ALTER TABLE operations_staff_task_receipt
  ADD COLUMN voice_url VARCHAR(512) DEFAULT NULL COMMENT '语音证据URL' AFTER photo_urls,
  ADD COLUMN voice_duration_sec INT DEFAULT NULL COMMENT '语音时长秒' AFTER voice_url;

ALTER TABLE incident_report
  ADD COLUMN voice_url VARCHAR(512) DEFAULT NULL COMMENT '语音证据URL' AFTER attachment_urls,
  ADD COLUMN voice_duration_sec INT DEFAULT NULL COMMENT '语音时长秒' AFTER voice_url;
