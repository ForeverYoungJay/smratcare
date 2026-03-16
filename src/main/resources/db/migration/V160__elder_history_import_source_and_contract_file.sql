ALTER TABLE elder
  ADD COLUMN source_type VARCHAR(32) NULL COMMENT '档案来源类型' AFTER remark,
  ADD COLUMN historical_contract_file_url VARCHAR(500) NULL COMMENT '历史合同附件地址' AFTER source_type;

ALTER TABLE elder_admission
  ADD COLUMN source_type VARCHAR(32) NULL COMMENT '入住来源类型' AFTER remark;
