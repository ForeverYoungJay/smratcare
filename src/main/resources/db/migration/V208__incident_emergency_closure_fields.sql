ALTER TABLE incident_report
  ADD COLUMN emergency_plan TEXT COMMENT '应急预案' AFTER action_taken,
  ADD COLUMN onsite_handling TEXT COMMENT '现场处置' AFTER emergency_plan,
  ADD COLUMN family_notification TEXT COMMENT '家属告知' AFTER onsite_handling,
  ADD COLUMN rectification_measure TEXT COMMENT '整改措施' AFTER family_notification,
  ADD COLUMN review_conclusion TEXT COMMENT '复盘结论' AFTER rectification_measure,
  ADD COLUMN regulatory_report TEXT COMMENT '监管上报' AFTER review_conclusion;
