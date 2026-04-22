-- 手工删除离职员工账号与人事资料
-- 默认策略：
-- 1. 仅处理指定机构下，staff.status = 0 或 staff_profile.status = 0 的员工
-- 2. 自动排除 admin / sysadmin_root
-- 3. 删除员工主档、角色、档案、培训、积分、排班考勤、宿舍/员工账单等直接关联数据
-- 4. 对业务历史表中可空的人员引用字段改为 NULL，避免删除员工后留下脏引用
--
-- 使用前先修改下面变量：
--   SET @target_org_id := 1;
-- 如需先预览，执行“预览候选员工”这一段即可。

SET @target_org_id := 1;

-- 预览候选员工
SELECT
  s.id,
  s.staff_no,
  s.username,
  s.real_name,
  s.status AS staff_status,
  sp.status AS profile_status,
  sp.leave_date,
  sp.leave_reason
FROM staff s
LEFT JOIN staff_profile sp
  ON sp.org_id = s.org_id
 AND sp.staff_id = s.id
 AND sp.is_deleted = 0
WHERE s.org_id = @target_org_id
  AND s.is_deleted = 0
  AND s.username NOT IN ('admin', 'sysadmin_root')
  AND (s.status = 0 OR COALESCE(sp.status, 1) = 0)
ORDER BY s.id;

-- 正式删除
START TRANSACTION;

CREATE TEMPORARY TABLE tmp_terminated_staff (
  staff_id BIGINT PRIMARY KEY
);

INSERT INTO tmp_terminated_staff (staff_id)
SELECT s.id
FROM staff s
LEFT JOIN staff_profile sp
  ON sp.org_id = s.org_id
 AND sp.staff_id = s.id
 AND sp.is_deleted = 0
WHERE s.org_id = @target_org_id
  AND s.is_deleted = 0
  AND s.username NOT IN ('admin', 'sysadmin_root')
  AND (s.status = 0 OR COALESCE(sp.status, 1) = 0);

-- 若需要，可先单独执行这句确认待删数量
SELECT COUNT(*) AS delete_candidate_count FROM tmp_terminated_staff;

-- 解除对离职员工的上级引用
UPDATE staff
SET direct_leader_id = NULL
WHERE org_id = @target_org_id
  AND is_deleted = 0
  AND direct_leader_id IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE staff
SET indirect_leader_id = NULL
WHERE org_id = @target_org_id
  AND is_deleted = 0
  AND indirect_leader_id IN (SELECT staff_id FROM tmp_terminated_staff);

-- 保留业务记录，但清空可空引用字段
UPDATE base_data_item
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE audit_log
SET actor_id = NULL
WHERE org_id = @target_org_id
  AND actor_id IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE building
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE floor
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE care_level
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE service_item
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE care_package
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE care_package_item
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE service_plan
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE service_booking
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE crm_lead
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE crm_marketing_plan
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE crm_contract
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE crm_callback_plan
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE crm_contract_attachment
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE crm_sms_task
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE crm_marketing_plan_receipt
SET staff_id = NULL
WHERE org_id = @target_org_id
  AND staff_id IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE crm_marketing_plan_performance
SET staff_id = NULL
WHERE org_id = @target_org_id
  AND staff_id IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE elder_admission
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE elder_change_log
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE elder_discharge
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE elder_account
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE elder_account_log
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE elder_outing_record
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE elder_trial_stay
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE elder_discharge_apply
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE elder_medical_outing_record
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE elder_death_register
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE elder_lifecycle_event
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE assessment_record
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE assessment_scale_template
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE medical_tcm_assessment
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE medical_cvd_risk_assessment
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE medical_alert_rule_config
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE health_basic_record
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE health_drug_dictionary
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE health_medication_deposit
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE health_medication_setting
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE health_medication_registration
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE health_archive
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE health_data_record
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE health_inspection
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE health_nursing_log
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE health_medication_task
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE dining_dish
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE dining_recipe
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE dining_prep_zone
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE dining_delivery_area
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE dining_meal_order
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE dining_delivery_record
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE dining_risk_threshold_config
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE dining_risk_intercept_log
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE dining_risk_override
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE meal_plan
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE activity_event
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE incident_report
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE one_card_account
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE life_room_cleaning_task
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE life_maintenance_request
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE fire_safety_record
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE logistics_equipment_archive
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE logistics_maintenance_todo_job_log
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE finance_admission_fee_audit
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE finance_discharge_fee_audit
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE finance_discharge_settlement
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE finance_consumption_record
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE finance_monthly_allocation
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE hr_staff_monthly_fee_bill
SET finance_sync_by = NULL
WHERE org_id = @target_org_id
  AND finance_sync_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE hr_staff_monthly_fee_bill
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE oa_notice
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE oa_todo
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE oa_approval
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE oa_document
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE oa_task
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE oa_document_folder
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE oa_suggestion
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE oa_work_report
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE oa_album
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE oa_knowledge
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE oa_group_setting
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE oa_activity_plan
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE report_record
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE survey_template
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE family_portal_state
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE family_recharge_order
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE family_sms_code_log
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

UPDATE family_notify_log
SET created_by = NULL
WHERE org_id = @target_org_id
  AND created_by IN (SELECT staff_id FROM tmp_terminated_staff);

-- 删除直接依赖 staff_id 的员工/HR 数据
DELETE FROM staff_role
WHERE org_id = @target_org_id
  AND staff_id IN (SELECT staff_id FROM tmp_terminated_staff);

DELETE FROM staff_profile
WHERE org_id = @target_org_id
  AND staff_id IN (SELECT staff_id FROM tmp_terminated_staff);

DELETE FROM staff_training_record
WHERE org_id = @target_org_id
  AND staff_id IN (SELECT staff_id FROM tmp_terminated_staff);

DELETE FROM staff_points_account
WHERE org_id = @target_org_id
  AND staff_id IN (SELECT staff_id FROM tmp_terminated_staff);

DELETE FROM staff_points_log
WHERE org_id = @target_org_id
  AND staff_id IN (SELECT staff_id FROM tmp_terminated_staff);

DELETE FROM staff_schedule
WHERE org_id = @target_org_id
  AND staff_id IN (SELECT staff_id FROM tmp_terminated_staff);

DELETE FROM attendance_record
WHERE org_id = @target_org_id
  AND staff_id IN (SELECT staff_id FROM tmp_terminated_staff);

DELETE FROM staff_reward_punishment
WHERE org_id = @target_org_id
  AND staff_id IN (SELECT staff_id FROM tmp_terminated_staff);

DELETE FROM hr_staff_service_plan
WHERE org_id = @target_org_id
  AND staff_id IN (SELECT staff_id FROM tmp_terminated_staff);

DELETE FROM hr_staff_monthly_fee_bill
WHERE org_id = @target_org_id
  AND staff_id IN (SELECT staff_id FROM tmp_terminated_staff);

DELETE FROM caregiver_group_member
WHERE staff_id IN (SELECT staff_id FROM tmp_terminated_staff);

DELETE FROM oa_quick_chat_state
WHERE org_id = @target_org_id
  AND staff_id IN (SELECT staff_id FROM tmp_terminated_staff);

DELETE FROM crm_marketing_plan_receipt
WHERE org_id = @target_org_id
  AND staff_id IN (SELECT staff_id FROM tmp_terminated_staff);

DELETE FROM crm_marketing_plan_performance
WHERE org_id = @target_org_id
  AND staff_id IN (SELECT staff_id FROM tmp_terminated_staff);

DELETE FROM shift_swap_request
WHERE org_id = @target_org_id
  AND (
    applicant_staff_id IN (SELECT staff_id FROM tmp_terminated_staff)
    OR target_staff_id IN (SELECT staff_id FROM tmp_terminated_staff)
  );

DELETE FROM staff
WHERE org_id = @target_org_id
  AND id IN (SELECT staff_id FROM tmp_terminated_staff);

DROP TEMPORARY TABLE tmp_terminated_staff;

COMMIT;
