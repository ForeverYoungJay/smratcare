SET @crm_contract_change_workflow_status_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_contract'
    AND COLUMN_NAME = 'change_workflow_status'
);
SET @crm_contract_change_workflow_status_sql = IF(
  @crm_contract_change_workflow_status_exists = 0,
  'ALTER TABLE crm_contract ADD COLUMN change_workflow_status VARCHAR(32) NOT NULL DEFAULT ''NONE'' COMMENT ''合同变更流程状态 NONE/IN_PROGRESS/PENDING_APPROVAL/APPROVED/REJECTED'' AFTER status',
  'SELECT 1'
);
PREPARE stmt FROM @crm_contract_change_workflow_status_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_contract_change_workflow_remark_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_contract'
    AND COLUMN_NAME = 'change_workflow_remark'
);
SET @crm_contract_change_workflow_remark_sql = IF(
  @crm_contract_change_workflow_remark_exists = 0,
  'ALTER TABLE crm_contract ADD COLUMN change_workflow_remark VARCHAR(255) DEFAULT NULL COMMENT ''合同变更说明'' AFTER change_workflow_status',
  'SELECT 1'
);
PREPARE stmt FROM @crm_contract_change_workflow_remark_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_callback_plan_callback_type_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_callback_plan'
    AND COLUMN_NAME = 'callback_type'
);
SET @crm_callback_plan_callback_type_sql = IF(
  @crm_callback_plan_callback_type_exists = 0,
  'ALTER TABLE crm_callback_plan ADD COLUMN callback_type VARCHAR(32) DEFAULT NULL COMMENT ''回访类型 CHECKIN/TRIAL/DISCHARGE/SCORE'' AFTER executor_name',
  'SELECT 1'
);
PREPARE stmt FROM @crm_callback_plan_callback_type_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_callback_plan_score_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'crm_callback_plan'
    AND COLUMN_NAME = 'score'
);
SET @crm_callback_plan_score_sql = IF(
  @crm_callback_plan_score_exists = 0,
  'ALTER TABLE crm_callback_plan ADD COLUMN score DECIMAL(4,1) DEFAULT NULL COMMENT ''回访评分 0-5'' AFTER callback_type',
  'SELECT 1'
);
PREPARE stmt FROM @crm_callback_plan_score_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE crm_contract
SET change_workflow_status = CASE
    WHEN contract_status = '变更中' THEN 'IN_PROGRESS'
    WHEN contract_status = '变更待审批' THEN 'PENDING_APPROVAL'
    WHEN contract_status = '变更已生效' THEN 'APPROVED'
    WHEN contract_status = '变更驳回' THEN 'REJECTED'
    ELSE COALESCE(change_workflow_status, 'NONE')
  END,
  change_workflow_remark = CASE
    WHEN contract_status IN ('变更中', '变更待审批', '变更已生效', '变更驳回')
      THEN COALESCE(change_workflow_remark, remark)
    ELSE change_workflow_remark
  END
WHERE COALESCE(change_workflow_status, 'NONE') = 'NONE'
   OR contract_status IN ('变更中', '变更待审批', '变更已生效', '变更驳回');

UPDATE crm_contract
SET status = CASE
    WHEN status = 'VOID' THEN 'VOID'
    WHEN flow_stage = 'SIGNED' THEN 'SIGNED'
    WHEN flow_stage = 'PENDING_SIGN' THEN 'APPROVED'
    WHEN flow_stage = 'PENDING_BED_SELECT' THEN 'PENDING_APPROVAL'
    ELSE 'DRAFT'
  END,
  contract_status = CASE
    WHEN flow_stage = 'SIGNED' OR status = 'SIGNED' OR status = 'EFFECTIVE' THEN '已审批,已通过'
    WHEN flow_stage = 'PENDING_SIGN' THEN '费用预审通过'
    WHEN flow_stage = 'PENDING_BED_SELECT' THEN '待办理入住'
    WHEN flow_stage = 'PENDING_ASSESSMENT' THEN '待评估'
    ELSE '未提交'
  END
WHERE change_workflow_status IN ('IN_PROGRESS', 'PENDING_APPROVAL', 'APPROVED', 'REJECTED');

UPDATE crm_callback_plan
SET callback_type = CASE
    WHEN CONCAT_WS(' ', title, followup_content, execute_note, followup_result) LIKE '%试住%' THEN 'TRIAL'
    WHEN CONCAT_WS(' ', title, followup_content, execute_note, followup_result) LIKE '%退住%'
      OR CONCAT_WS(' ', title, followup_content, execute_note, followup_result) LIKE '%离院%'
      OR CONCAT_WS(' ', title, followup_content, execute_note, followup_result) LIKE '%出院%' THEN 'DISCHARGE'
    WHEN CONCAT_WS(' ', title, followup_content, execute_note, followup_result) LIKE '%评分%'
      OR CONCAT_WS(' ', title, followup_content, execute_note, followup_result) LIKE '%满意度%'
      OR CONCAT_WS(' ', title, followup_content, execute_note, followup_result) LIKE '%星级%' THEN 'SCORE'
    WHEN callback_type IS NULL OR callback_type = '' THEN 'CHECKIN'
    ELSE callback_type
  END
WHERE callback_type IS NULL OR callback_type = '';
