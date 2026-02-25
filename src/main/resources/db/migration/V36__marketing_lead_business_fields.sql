ALTER TABLE crm_lead
  ADD COLUMN IF NOT EXISTS consultant_name VARCHAR(64) DEFAULT NULL COMMENT '咨询人姓名' AFTER phone,
  ADD COLUMN IF NOT EXISTS consultant_phone VARCHAR(32) DEFAULT NULL COMMENT '咨询人联系电话' AFTER consultant_name,
  ADD COLUMN IF NOT EXISTS elder_name VARCHAR(64) DEFAULT NULL COMMENT '老人姓名' AFTER consultant_phone,
  ADD COLUMN IF NOT EXISTS elder_phone VARCHAR(32) DEFAULT NULL COMMENT '老人联系电话' AFTER elder_name,
  ADD COLUMN IF NOT EXISTS gender TINYINT DEFAULT NULL COMMENT '性别 1男 2女' AFTER elder_phone,
  ADD COLUMN IF NOT EXISTS age INT DEFAULT NULL COMMENT '年龄' AFTER gender,
  ADD COLUMN IF NOT EXISTS consult_date DATE DEFAULT NULL COMMENT '咨询日期' AFTER age,
  ADD COLUMN IF NOT EXISTS consult_type VARCHAR(64) DEFAULT NULL COMMENT '咨询类型' AFTER consult_date,
  ADD COLUMN IF NOT EXISTS media_channel VARCHAR(64) DEFAULT NULL COMMENT '媒体渠道' AFTER consult_type,
  ADD COLUMN IF NOT EXISTS info_source VARCHAR(64) DEFAULT NULL COMMENT '信息来源' AFTER media_channel,
  ADD COLUMN IF NOT EXISTS receptionist_name VARCHAR(64) DEFAULT NULL COMMENT '接待人' AFTER info_source,
  ADD COLUMN IF NOT EXISTS home_address VARCHAR(255) DEFAULT NULL COMMENT '家庭地址' AFTER receptionist_name,
  ADD COLUMN IF NOT EXISTS marketer_name VARCHAR(64) DEFAULT NULL COMMENT '归属营销人员' AFTER home_address,
  ADD COLUMN IF NOT EXISTS followup_status VARCHAR(32) DEFAULT NULL COMMENT '回访状态' AFTER marketer_name,
  ADD COLUMN IF NOT EXISTS referral_channel VARCHAR(64) DEFAULT NULL COMMENT '推荐渠道' AFTER followup_status,
  ADD COLUMN IF NOT EXISTS invalid_time DATETIME DEFAULT NULL COMMENT '失效时间' AFTER referral_channel,
  ADD COLUMN IF NOT EXISTS id_card_no VARCHAR(32) DEFAULT NULL COMMENT '身份证号' AFTER invalid_time,
  ADD COLUMN IF NOT EXISTS reservation_amount DECIMAL(12, 2) DEFAULT NULL COMMENT '预订金额' AFTER id_card_no,
  ADD COLUMN IF NOT EXISTS reservation_room_no VARCHAR(64) DEFAULT NULL COMMENT '预订房号' AFTER reservation_amount,
  ADD COLUMN IF NOT EXISTS payment_time DATETIME DEFAULT NULL COMMENT '收款时间' AFTER reservation_room_no,
  ADD COLUMN IF NOT EXISTS refunded TINYINT NOT NULL DEFAULT 0 COMMENT '是否退款 0否1是' AFTER payment_time,
  ADD COLUMN IF NOT EXISTS reservation_channel VARCHAR(64) DEFAULT NULL COMMENT '预约渠道' AFTER refunded,
  ADD COLUMN IF NOT EXISTS reservation_status VARCHAR(32) DEFAULT NULL COMMENT '预订状态' AFTER reservation_channel,
  ADD COLUMN IF NOT EXISTS org_name VARCHAR(64) DEFAULT NULL COMMENT '所属机构' AFTER reservation_status,
  ADD COLUMN IF NOT EXISTS contract_status VARCHAR(64) DEFAULT NULL COMMENT '合同状态' AFTER contract_no,
  ADD COLUMN IF NOT EXISTS contract_expiry_date DATE DEFAULT NULL COMMENT '合同有效期止' AFTER contract_status,
  ADD COLUMN IF NOT EXISTS sms_send_count INT NOT NULL DEFAULT 0 COMMENT '短信发送次数' AFTER contract_expiry_date;

CREATE INDEX IF NOT EXISTS idx_crm_lead_consult_date ON crm_lead (tenant_id, is_deleted, consult_date);
CREATE INDEX IF NOT EXISTS idx_crm_lead_marketer ON crm_lead (tenant_id, is_deleted, marketer_name);
CREATE INDEX IF NOT EXISTS idx_crm_lead_contract_status ON crm_lead (tenant_id, is_deleted, contract_status);
