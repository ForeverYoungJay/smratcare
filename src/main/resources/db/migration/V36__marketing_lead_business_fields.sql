-- Marketing lead business fields (idempotent)

SET @crm_lead_consultant_name_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_lead'
    AND COLUMN_NAME = 'consultant_name'
);
SET @ddl = IF(
  @crm_lead_consultant_name_exists = 0,
  'ALTER TABLE crm_lead ADD COLUMN consultant_name VARCHAR(64) DEFAULT NULL COMMENT ''咨询人姓名''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_lead_consultant_phone_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_lead'
    AND COLUMN_NAME = 'consultant_phone'
);
SET @ddl = IF(
  @crm_lead_consultant_phone_exists = 0,
  'ALTER TABLE crm_lead ADD COLUMN consultant_phone VARCHAR(32) DEFAULT NULL COMMENT ''咨询人联系电话''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_lead_elder_name_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_lead'
    AND COLUMN_NAME = 'elder_name'
);
SET @ddl = IF(
  @crm_lead_elder_name_exists = 0,
  'ALTER TABLE crm_lead ADD COLUMN elder_name VARCHAR(64) DEFAULT NULL COMMENT ''老人姓名''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_lead_elder_phone_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_lead'
    AND COLUMN_NAME = 'elder_phone'
);
SET @ddl = IF(
  @crm_lead_elder_phone_exists = 0,
  'ALTER TABLE crm_lead ADD COLUMN elder_phone VARCHAR(32) DEFAULT NULL COMMENT ''老人联系电话''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_lead_gender_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_lead'
    AND COLUMN_NAME = 'gender'
);
SET @ddl = IF(
  @crm_lead_gender_exists = 0,
  'ALTER TABLE crm_lead ADD COLUMN gender TINYINT DEFAULT NULL COMMENT ''性别 1男 2女''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_lead_age_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_lead'
    AND COLUMN_NAME = 'age'
);
SET @ddl = IF(
  @crm_lead_age_exists = 0,
  'ALTER TABLE crm_lead ADD COLUMN age INT DEFAULT NULL COMMENT ''年龄''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_lead_consult_date_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_lead'
    AND COLUMN_NAME = 'consult_date'
);
SET @ddl = IF(
  @crm_lead_consult_date_exists = 0,
  'ALTER TABLE crm_lead ADD COLUMN consult_date DATE DEFAULT NULL COMMENT ''咨询日期''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_lead_consult_type_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_lead'
    AND COLUMN_NAME = 'consult_type'
);
SET @ddl = IF(
  @crm_lead_consult_type_exists = 0,
  'ALTER TABLE crm_lead ADD COLUMN consult_type VARCHAR(64) DEFAULT NULL COMMENT ''咨询类型''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_lead_media_channel_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_lead'
    AND COLUMN_NAME = 'media_channel'
);
SET @ddl = IF(
  @crm_lead_media_channel_exists = 0,
  'ALTER TABLE crm_lead ADD COLUMN media_channel VARCHAR(64) DEFAULT NULL COMMENT ''媒体渠道''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_lead_info_source_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_lead'
    AND COLUMN_NAME = 'info_source'
);
SET @ddl = IF(
  @crm_lead_info_source_exists = 0,
  'ALTER TABLE crm_lead ADD COLUMN info_source VARCHAR(64) DEFAULT NULL COMMENT ''信息来源''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_lead_receptionist_name_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_lead'
    AND COLUMN_NAME = 'receptionist_name'
);
SET @ddl = IF(
  @crm_lead_receptionist_name_exists = 0,
  'ALTER TABLE crm_lead ADD COLUMN receptionist_name VARCHAR(64) DEFAULT NULL COMMENT ''接待人''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_lead_home_address_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_lead'
    AND COLUMN_NAME = 'home_address'
);
SET @ddl = IF(
  @crm_lead_home_address_exists = 0,
  'ALTER TABLE crm_lead ADD COLUMN home_address VARCHAR(255) DEFAULT NULL COMMENT ''家庭地址''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_lead_marketer_name_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_lead'
    AND COLUMN_NAME = 'marketer_name'
);
SET @ddl = IF(
  @crm_lead_marketer_name_exists = 0,
  'ALTER TABLE crm_lead ADD COLUMN marketer_name VARCHAR(64) DEFAULT NULL COMMENT ''归属营销人员''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_lead_followup_status_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_lead'
    AND COLUMN_NAME = 'followup_status'
);
SET @ddl = IF(
  @crm_lead_followup_status_exists = 0,
  'ALTER TABLE crm_lead ADD COLUMN followup_status VARCHAR(32) DEFAULT NULL COMMENT ''回访状态''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_lead_referral_channel_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_lead'
    AND COLUMN_NAME = 'referral_channel'
);
SET @ddl = IF(
  @crm_lead_referral_channel_exists = 0,
  'ALTER TABLE crm_lead ADD COLUMN referral_channel VARCHAR(64) DEFAULT NULL COMMENT ''推荐渠道''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_lead_invalid_time_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_lead'
    AND COLUMN_NAME = 'invalid_time'
);
SET @ddl = IF(
  @crm_lead_invalid_time_exists = 0,
  'ALTER TABLE crm_lead ADD COLUMN invalid_time DATETIME DEFAULT NULL COMMENT ''失效时间''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_lead_id_card_no_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_lead'
    AND COLUMN_NAME = 'id_card_no'
);
SET @ddl = IF(
  @crm_lead_id_card_no_exists = 0,
  'ALTER TABLE crm_lead ADD COLUMN id_card_no VARCHAR(32) DEFAULT NULL COMMENT ''身份证号''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_lead_reservation_amount_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_lead'
    AND COLUMN_NAME = 'reservation_amount'
);
SET @ddl = IF(
  @crm_lead_reservation_amount_exists = 0,
  'ALTER TABLE crm_lead ADD COLUMN reservation_amount DECIMAL(12, 2) DEFAULT NULL COMMENT ''预订金额''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_lead_reservation_room_no_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_lead'
    AND COLUMN_NAME = 'reservation_room_no'
);
SET @ddl = IF(
  @crm_lead_reservation_room_no_exists = 0,
  'ALTER TABLE crm_lead ADD COLUMN reservation_room_no VARCHAR(64) DEFAULT NULL COMMENT ''预订房号''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_lead_payment_time_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_lead'
    AND COLUMN_NAME = 'payment_time'
);
SET @ddl = IF(
  @crm_lead_payment_time_exists = 0,
  'ALTER TABLE crm_lead ADD COLUMN payment_time DATETIME DEFAULT NULL COMMENT ''收款时间''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_lead_refunded_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_lead'
    AND COLUMN_NAME = 'refunded'
);
SET @ddl = IF(
  @crm_lead_refunded_exists = 0,
  'ALTER TABLE crm_lead ADD COLUMN refunded TINYINT NOT NULL DEFAULT 0 COMMENT ''是否退款 0否1是''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_lead_reservation_channel_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_lead'
    AND COLUMN_NAME = 'reservation_channel'
);
SET @ddl = IF(
  @crm_lead_reservation_channel_exists = 0,
  'ALTER TABLE crm_lead ADD COLUMN reservation_channel VARCHAR(64) DEFAULT NULL COMMENT ''预约渠道''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_lead_reservation_status_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_lead'
    AND COLUMN_NAME = 'reservation_status'
);
SET @ddl = IF(
  @crm_lead_reservation_status_exists = 0,
  'ALTER TABLE crm_lead ADD COLUMN reservation_status VARCHAR(32) DEFAULT NULL COMMENT ''预订状态''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_lead_org_name_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_lead'
    AND COLUMN_NAME = 'org_name'
);
SET @ddl = IF(
  @crm_lead_org_name_exists = 0,
  'ALTER TABLE crm_lead ADD COLUMN org_name VARCHAR(64) DEFAULT NULL COMMENT ''所属机构''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_lead_contract_status_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_lead'
    AND COLUMN_NAME = 'contract_status'
);
SET @ddl = IF(
  @crm_lead_contract_status_exists = 0,
  'ALTER TABLE crm_lead ADD COLUMN contract_status VARCHAR(64) DEFAULT NULL COMMENT ''合同状态''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_lead_contract_expiry_date_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_lead'
    AND COLUMN_NAME = 'contract_expiry_date'
);
SET @ddl = IF(
  @crm_lead_contract_expiry_date_exists = 0,
  'ALTER TABLE crm_lead ADD COLUMN contract_expiry_date DATE DEFAULT NULL COMMENT ''合同有效期止''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_lead_sms_send_count_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_lead'
    AND COLUMN_NAME = 'sms_send_count'
);
SET @ddl = IF(
  @crm_lead_sms_send_count_exists = 0,
  'ALTER TABLE crm_lead ADD COLUMN sms_send_count INT NOT NULL DEFAULT 0 COMMENT ''短信发送次数''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_crm_lead_consult_date_exists = (
  SELECT COUNT(1)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_lead'
    AND INDEX_NAME = 'idx_crm_lead_consult_date'
);
SET @ddl = IF(
  @idx_crm_lead_consult_date_exists = 0,
  'CREATE INDEX idx_crm_lead_consult_date ON crm_lead (tenant_id, is_deleted, consult_date)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_crm_lead_marketer_exists = (
  SELECT COUNT(1)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_lead'
    AND INDEX_NAME = 'idx_crm_lead_marketer'
);
SET @ddl = IF(
  @idx_crm_lead_marketer_exists = 0,
  'CREATE INDEX idx_crm_lead_marketer ON crm_lead (tenant_id, is_deleted, marketer_name)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_crm_lead_contract_status_exists = (
  SELECT COUNT(1)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_lead'
    AND INDEX_NAME = 'idx_crm_lead_contract_status'
);
SET @ddl = IF(
  @idx_crm_lead_contract_status_exists = 0,
  'CREATE INDEX idx_crm_lead_contract_status ON crm_lead (tenant_id, is_deleted, contract_status)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
