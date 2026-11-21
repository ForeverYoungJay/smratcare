-- 合同台账补充：合同类型、参考月费（仅签约参考，实际收费以财务审批为准）、预计入住日
ALTER TABLE crm_contract
  ADD COLUMN contract_type VARCHAR(32) DEFAULT NULL COMMENT '合同类型 LONG_TERM/SHORT_TERM/TRIAL/DAY_CARE',
  ADD COLUMN reference_monthly_fee DECIMAL(12,2) DEFAULT NULL COMMENT '参考月费（元）',
  ADD COLUMN expected_admission_date DATE DEFAULT NULL COMMENT '预计入住日';
