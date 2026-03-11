ALTER TABLE staff_profile
  ADD COLUMN contract_no VARCHAR(64) DEFAULT NULL COMMENT '劳动合同编号' AFTER employment_type,
  ADD COLUMN contract_type VARCHAR(32) DEFAULT NULL COMMENT '合同类型' AFTER contract_no,
  ADD COLUMN contract_status VARCHAR(32) DEFAULT NULL COMMENT '合同状态' AFTER contract_type,
  ADD COLUMN contract_start_date DATE DEFAULT NULL COMMENT '合同开始日期' AFTER contract_status,
  ADD COLUMN contract_end_date DATE DEFAULT NULL COMMENT '合同结束日期' AFTER contract_start_date;
