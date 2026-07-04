ALTER TABLE health_archive
  ADD COLUMN recent_medical_visit TEXT COMMENT '最近就医记录' AFTER medical_history,
  ADD COLUMN check_report_summary TEXT COMMENT '检查报告摘要' AFTER recent_medical_visit,
  ADD COLUMN rehabilitation_record TEXT COMMENT '康复计划/记录' AFTER check_report_summary;
