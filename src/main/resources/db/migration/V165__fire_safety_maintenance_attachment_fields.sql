ALTER TABLE fire_safety_record
  ADD COLUMN third_party_maintenance_file_url VARCHAR(512) NULL COMMENT '第三方维保记录单' AFTER image_urls,
  ADD COLUMN purchase_contract_file_url VARCHAR(512) NULL COMMENT '采购合同附件' AFTER third_party_maintenance_file_url,
  ADD COLUMN contract_start_date DATE NULL COMMENT '合约开始日期' AFTER purchase_contract_file_url,
  ADD COLUMN contract_end_date DATE NULL COMMENT '合约结束日期' AFTER contract_start_date,
  ADD COLUMN purchase_document_urls TEXT NULL COMMENT '采购单据附件，按换行分隔' AFTER contract_end_date;
