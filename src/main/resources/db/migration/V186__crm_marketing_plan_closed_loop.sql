ALTER TABLE crm_marketing_plan
  ADD COLUMN campaign_code VARCHAR(64) DEFAULT NULL COMMENT '活动编码' AFTER owner,
  ADD COLUMN source_tag VARCHAR(64) DEFAULT NULL COMMENT '来源标签' AFTER campaign_code,
  ADD COLUMN budget_amount DECIMAL(12,2) DEFAULT NULL COMMENT '预算金额' AFTER source_tag,
  ADD COLUMN target_lead_count INT DEFAULT NULL COMMENT '目标线索数' AFTER budget_amount,
  ADD COLUMN target_reservation_count INT DEFAULT NULL COMMENT '目标预定数' AFTER target_lead_count,
  ADD COLUMN target_contract_count INT DEFAULT NULL COMMENT '目标签约数' AFTER target_reservation_count;

CREATE INDEX idx_crm_marketing_plan_source_tag ON crm_marketing_plan(source_tag);
