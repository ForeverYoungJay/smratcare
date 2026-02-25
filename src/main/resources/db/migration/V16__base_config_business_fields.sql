ALTER TABLE crm_lead
  ADD COLUMN customer_tag VARCHAR(64) DEFAULT NULL COMMENT '客户标签' AFTER source;

ALTER TABLE elder_trial_stay
  ADD COLUMN trial_package VARCHAR(64) DEFAULT NULL COMMENT '试住套餐' AFTER channel;
