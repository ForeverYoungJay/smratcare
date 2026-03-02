-- CRM合同域补全：合同投影字段、附件/短信按合同关联

SET @crm_contract_reservation_room_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_contract'
    AND COLUMN_NAME = 'reservation_room_no'
);
SET @crm_contract_reservation_room_sql = IF(
  @crm_contract_reservation_room_exists = 0,
  'ALTER TABLE crm_contract ADD COLUMN reservation_room_no VARCHAR(64) DEFAULT NULL COMMENT ''签约房号'' AFTER snapshot_json',
  'SELECT 1'
);
PREPARE stmt FROM @crm_contract_reservation_room_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_contract_name_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_contract'
    AND COLUMN_NAME = 'name'
);
SET @crm_contract_name_sql = IF(
  @crm_contract_name_exists = 0,
  'ALTER TABLE crm_contract ADD COLUMN name VARCHAR(64) DEFAULT NULL COMMENT ''咨询人姓名'' AFTER contract_no',
  'SELECT 1'
);
PREPARE stmt FROM @crm_contract_name_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_contract_phone_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_contract'
    AND COLUMN_NAME = 'phone'
);
SET @crm_contract_phone_sql = IF(
  @crm_contract_phone_exists = 0,
  'ALTER TABLE crm_contract ADD COLUMN phone VARCHAR(32) DEFAULT NULL COMMENT ''咨询人联系电话'' AFTER name',
  'SELECT 1'
);
PREPARE stmt FROM @crm_contract_phone_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_contract_id_card_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_contract'
    AND COLUMN_NAME = 'id_card_no'
);
SET @crm_contract_id_card_sql = IF(
  @crm_contract_id_card_exists = 0,
  'ALTER TABLE crm_contract ADD COLUMN id_card_no VARCHAR(32) DEFAULT NULL COMMENT ''身份证号'' AFTER phone',
  'SELECT 1'
);
PREPARE stmt FROM @crm_contract_id_card_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_contract_home_address_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_contract'
    AND COLUMN_NAME = 'home_address'
);
SET @crm_contract_home_address_sql = IF(
  @crm_contract_home_address_exists = 0,
  'ALTER TABLE crm_contract ADD COLUMN home_address VARCHAR(255) DEFAULT NULL COMMENT ''家庭住址'' AFTER id_card_no',
  'SELECT 1'
);
PREPARE stmt FROM @crm_contract_home_address_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_contract_elder_name_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_contract'
    AND COLUMN_NAME = 'elder_name'
);
SET @crm_contract_elder_name_sql = IF(
  @crm_contract_elder_name_exists = 0,
  'ALTER TABLE crm_contract ADD COLUMN elder_name VARCHAR(64) DEFAULT NULL COMMENT ''长者姓名'' AFTER reservation_room_no',
  'SELECT 1'
);
PREPARE stmt FROM @crm_contract_elder_name_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_contract_reservation_bed_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_contract'
    AND COLUMN_NAME = 'reservation_bed_id'
);
SET @crm_contract_reservation_bed_sql = IF(
  @crm_contract_reservation_bed_exists = 0,
  'ALTER TABLE crm_contract ADD COLUMN reservation_bed_id BIGINT DEFAULT NULL COMMENT ''预订床位ID'' AFTER reservation_room_no',
  'SELECT 1'
);
PREPARE stmt FROM @crm_contract_reservation_bed_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_contract_elder_phone_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_contract'
    AND COLUMN_NAME = 'elder_phone'
);
SET @crm_contract_elder_phone_sql = IF(
  @crm_contract_elder_phone_exists = 0,
  'ALTER TABLE crm_contract ADD COLUMN elder_phone VARCHAR(32) DEFAULT NULL COMMENT ''长者联系电话'' AFTER elder_name',
  'SELECT 1'
);
PREPARE stmt FROM @crm_contract_elder_phone_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_contract_gender_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_contract'
    AND COLUMN_NAME = 'gender'
);
SET @crm_contract_gender_sql = IF(
  @crm_contract_gender_exists = 0,
  'ALTER TABLE crm_contract ADD COLUMN gender TINYINT DEFAULT NULL COMMENT ''性别'' AFTER elder_phone',
  'SELECT 1'
);
PREPARE stmt FROM @crm_contract_gender_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_contract_age_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_contract'
    AND COLUMN_NAME = 'age'
);
SET @crm_contract_age_sql = IF(
  @crm_contract_age_exists = 0,
  'ALTER TABLE crm_contract ADD COLUMN age INT DEFAULT NULL COMMENT ''年龄'' AFTER gender',
  'SELECT 1'
);
PREPARE stmt FROM @crm_contract_age_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_contract_contract_status_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_contract'
    AND COLUMN_NAME = 'contract_status'
);
SET @crm_contract_contract_status_sql = IF(
  @crm_contract_contract_status_exists = 0,
  'ALTER TABLE crm_contract ADD COLUMN contract_status VARCHAR(64) DEFAULT NULL COMMENT ''展示合同状态'' AFTER age',
  'SELECT 1'
);
PREPARE stmt FROM @crm_contract_contract_status_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_contract_flow_stage_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_contract'
    AND COLUMN_NAME = 'flow_stage'
);
SET @crm_contract_flow_stage_sql = IF(
  @crm_contract_flow_stage_exists = 0,
  'ALTER TABLE crm_contract ADD COLUMN flow_stage VARCHAR(32) DEFAULT NULL COMMENT ''流程阶段'' AFTER contract_status',
  'SELECT 1'
);
PREPARE stmt FROM @crm_contract_flow_stage_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_contract_owner_dept_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_contract'
    AND COLUMN_NAME = 'current_owner_dept'
);
SET @crm_contract_owner_dept_sql = IF(
  @crm_contract_owner_dept_exists = 0,
  'ALTER TABLE crm_contract ADD COLUMN current_owner_dept VARCHAR(32) DEFAULT NULL COMMENT ''当前责任部门'' AFTER flow_stage',
  'SELECT 1'
);
PREPARE stmt FROM @crm_contract_owner_dept_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_contract_org_name_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_contract'
    AND COLUMN_NAME = 'org_name'
);
SET @crm_contract_org_name_sql = IF(
  @crm_contract_org_name_exists = 0,
  'ALTER TABLE crm_contract ADD COLUMN org_name VARCHAR(64) DEFAULT NULL COMMENT ''所属机构'' AFTER current_owner_dept',
  'SELECT 1'
);
PREPARE stmt FROM @crm_contract_org_name_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_contract_sms_send_count_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_contract'
    AND COLUMN_NAME = 'sms_send_count'
);
SET @crm_contract_sms_send_count_sql = IF(
  @crm_contract_sms_send_count_exists = 0,
  'ALTER TABLE crm_contract ADD COLUMN sms_send_count INT NOT NULL DEFAULT 0 COMMENT ''短信发送次数'' AFTER org_name',
  'SELECT 1'
);
PREPARE stmt FROM @crm_contract_sms_send_count_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_contract_remark_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_contract'
    AND COLUMN_NAME = 'remark'
);
SET @crm_contract_remark_sql = IF(
  @crm_contract_remark_exists = 0,
  'ALTER TABLE crm_contract ADD COLUMN remark VARCHAR(255) DEFAULT NULL COMMENT ''备注'' AFTER sms_send_count',
  'SELECT 1'
);
PREPARE stmt FROM @crm_contract_remark_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_sms_task_contract_id_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_sms_task'
    AND COLUMN_NAME = 'contract_id'
);
SET @crm_sms_task_contract_id_sql = IF(
  @crm_sms_task_contract_id_exists = 0,
  'ALTER TABLE crm_sms_task ADD COLUMN contract_id BIGINT DEFAULT NULL COMMENT ''合同ID'' AFTER lead_id',
  'SELECT 1'
);
PREPARE stmt FROM @crm_sms_task_contract_id_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_sms_task_contract_idx_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_sms_task'
    AND INDEX_NAME = 'idx_crm_sms_task_contract'
);
SET @crm_sms_task_contract_idx_sql = IF(
  @crm_sms_task_contract_idx_exists = 0,
  'ALTER TABLE crm_sms_task ADD KEY idx_crm_sms_task_contract (tenant_id, contract_id, is_deleted)',
  'SELECT 1'
);
PREPARE stmt FROM @crm_sms_task_contract_idx_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE crm_contract c
JOIN crm_lead l ON l.tenant_id = c.tenant_id
  AND l.is_deleted = 0
  AND (l.id = c.lead_id OR l.contract_no = c.contract_no)
SET c.reservation_room_no = COALESCE(c.reservation_room_no, l.reservation_room_no),
    c.reservation_bed_id = COALESCE(c.reservation_bed_id, l.reservation_bed_id),
    c.name = COALESCE(c.name, l.name, l.elder_name),
    c.phone = COALESCE(c.phone, l.phone, l.elder_phone),
    c.id_card_no = COALESCE(c.id_card_no, l.id_card_no),
    c.home_address = COALESCE(c.home_address, l.home_address),
    c.elder_name = COALESCE(c.elder_name, l.elder_name, l.name),
    c.elder_phone = COALESCE(c.elder_phone, l.elder_phone, l.phone),
    c.gender = COALESCE(c.gender, l.gender),
    c.age = COALESCE(c.age, l.age),
    c.contract_status = COALESCE(c.contract_status, l.contract_status),
    c.flow_stage = COALESCE(c.flow_stage, l.flow_stage),
    c.current_owner_dept = COALESCE(c.current_owner_dept, l.current_owner_dept),
    c.org_name = COALESCE(c.org_name, l.org_name),
    c.sms_send_count = COALESCE(c.sms_send_count, l.sms_send_count, 0),
    c.marketer_name = COALESCE(c.marketer_name, l.marketer_name),
    c.signed_at = COALESCE(c.signed_at, l.contract_signed_at),
    c.effective_to = COALESCE(c.effective_to, l.contract_expiry_date),
    c.status = CASE
      WHEN c.status IS NOT NULL AND c.status <> '' THEN c.status
      WHEN l.contract_signed_flag = 1 THEN 'SIGNED'
      WHEN l.flow_stage = 'PENDING_SIGN' THEN 'APPROVED'
      WHEN l.flow_stage = 'PENDING_BED_SELECT' THEN 'PENDING_APPROVAL'
      ELSE 'DRAFT'
    END;

INSERT INTO crm_contract (
  id, tenant_id, org_id, lead_id, elder_id, contract_no, status, signed_at, effective_from, effective_to,
  marketer_name, snapshot_json, name, phone, id_card_no, home_address, reservation_room_no, reservation_bed_id,
  elder_name, elder_phone, gender, age,
  contract_status, flow_stage, current_owner_dept, org_name, sms_send_count, remark, created_by,
  create_time, update_time, is_deleted
)
SELECT
  (900000000000000000 + l.id) AS id,
  l.tenant_id,
  l.org_id,
  l.id,
  NULL,
  l.contract_no,
  CASE
    WHEN l.contract_signed_flag = 1 THEN 'SIGNED'
    WHEN l.flow_stage = 'PENDING_SIGN' THEN 'APPROVED'
    WHEN l.flow_stage = 'PENDING_BED_SELECT' THEN 'PENDING_APPROVAL'
    ELSE 'DRAFT'
  END,
  l.contract_signed_at,
  DATE(l.contract_signed_at),
  l.contract_expiry_date,
  l.marketer_name,
  NULL,
  l.name,
  l.phone,
  l.id_card_no,
  l.home_address,
  l.reservation_room_no,
  l.reservation_bed_id,
  COALESCE(l.elder_name, l.name),
  COALESCE(l.elder_phone, l.phone),
  l.gender,
  l.age,
  l.contract_status,
  l.flow_stage,
  l.current_owner_dept,
  l.org_name,
  COALESCE(l.sms_send_count, 0),
  l.remark,
  l.created_by,
  COALESCE(l.create_time, CURRENT_TIMESTAMP),
  COALESCE(l.update_time, CURRENT_TIMESTAMP),
  0
FROM crm_lead l
LEFT JOIN crm_contract c
  ON c.tenant_id = l.tenant_id
  AND c.is_deleted = 0
  AND c.contract_no = l.contract_no
WHERE l.is_deleted = 0
  AND l.contract_no IS NOT NULL
  AND l.contract_no <> ''
  AND c.id IS NULL;

UPDATE crm_contract_attachment a
JOIN crm_contract c ON c.tenant_id = a.tenant_id
  AND c.is_deleted = 0
  AND c.contract_no = a.contract_no
SET a.contract_id = COALESCE(a.contract_id, c.id)
WHERE a.is_deleted = 0;

UPDATE crm_sms_task t
LEFT JOIN crm_contract c ON c.tenant_id = t.tenant_id
  AND c.is_deleted = 0
  AND (c.lead_id = t.lead_id OR (c.contract_no IS NOT NULL AND c.contract_no <> '' AND c.contract_no = (
    SELECT l.contract_no FROM crm_lead l WHERE l.id = t.lead_id LIMIT 1
  )))
SET t.contract_id = COALESCE(t.contract_id, c.id)
WHERE t.is_deleted = 0;
