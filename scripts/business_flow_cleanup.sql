-- 清理 business_flow_e2e.py / ops_flow_e2e.py 产生的测试数据（只在本地/测试库执行）。
-- 用法：docker compose exec -T mysql mysql --default-character-set=utf8mb4 -uroot -p"$MYSQL_ROOT_PASSWORD" "$MYSQL_DATABASE" < scripts/business_flow_cleanup.sql
-- 识别方式：名称以「流程测试」开头、账号以 flow 开头、工资期间 2099-01、remark 为「流程测试临时」的计费配置。
-- 注意：如运行过收款步骤，本月由脚本触发生成的其他长者账单需另行核对（按 create_time 识别）。
START TRANSACTION;

-- 住户线
CREATE TEMPORARY TABLE t_e AS SELECT id FROM elder WHERE full_name LIKE '流程测试%';
CREATE TEMPORARY TABLE t_f AS SELECT id FROM family_user WHERE real_name LIKE '流程测试%';
DELETE FROM care_incident_action WHERE incident_id IN (SELECT id FROM care_incident WHERE elder_id IN (SELECT id FROM t_e));
DELETE FROM visit_booking WHERE elder_id IN (SELECT id FROM t_e) OR family_user_id IN (SELECT id FROM t_f);
DELETE FROM family_recharge_order WHERE elder_id IN (SELECT id FROM t_e) OR family_user_id IN (SELECT id FROM t_f);
DELETE FROM family_notify_log WHERE elder_id IN (SELECT id FROM t_e) OR family_user_id IN (SELECT id FROM t_f);
DELETE FROM health_medication_registration WHERE elder_id IN (SELECT id FROM t_e);
DELETE FROM health_medication_task WHERE elder_id IN (SELECT id FROM t_e);
DELETE FROM health_medication_setting WHERE elder_id IN (SELECT id FROM t_e);
CREATE TEMPORARY TABLE t_l AS SELECT id FROM crm_lead WHERE name LIKE '流程测试%' OR elder_name LIKE '流程测试%';
CREATE TEMPORARY TABLE t_c AS SELECT id FROM crm_contract WHERE elder_name LIKE '流程测试%' OR lead_id IN (SELECT id FROM t_l);
CREATE TEMPORARY TABLE t_b AS SELECT id FROM bill_monthly WHERE elder_id IN (SELECT id FROM t_e);
CREATE TEMPORARY TABLE t_s AS SELECT id FROM finance_discharge_settlement WHERE elder_id IN (SELECT id FROM t_e);
CREATE TEMPORARY TABLE t_t AS SELECT id FROM care_task_daily WHERE elder_id IN (SELECT id FROM t_e);
CREATE TEMPORARY TABLE t_m AS SELECT id FROM medical_order WHERE elder_id IN (SELECT id FROM t_e);
DELETE FROM bill_item WHERE bill_monthly_id IN (SELECT id FROM t_b);
DELETE FROM payment_record WHERE bill_monthly_id IN (SELECT id FROM t_b);
DELETE FROM finance_consumer_voucher_usage WHERE bill_monthly_id IN (SELECT id FROM t_b);
DELETE FROM bill_monthly WHERE id IN (SELECT id FROM t_b);
DELETE FROM finance_refund_voucher WHERE settlement_id IN (SELECT id FROM t_s) OR elder_id IN (SELECT id FROM t_e);
DELETE FROM care_task_review WHERE task_daily_id IN (SELECT id FROM t_t);
DELETE FROM care_task_execute_log WHERE task_daily_id IN (SELECT id FROM t_t) OR elder_id IN (SELECT id FROM t_e);
DELETE FROM medical_order_execution WHERE order_id IN (SELECT id FROM t_m) OR elder_id IN (SELECT id FROM t_e);
DELETE FROM medical_rounds_record WHERE medical_order_id IN (SELECT id FROM t_m) OR elder_id IN (SELECT id FROM t_e);
UPDATE bed SET elder_id = NULL, status = 1 WHERE elder_id IN (SELECT id FROM t_e);
DELETE FROM ai_risk_score WHERE elder_id IN (SELECT id FROM t_e);
DELETE FROM assessment_record WHERE elder_id IN (SELECT id FROM t_e);
DELETE FROM care_incident WHERE elder_id IN (SELECT id FROM t_e);
DELETE FROM care_task_daily WHERE elder_id IN (SELECT id FROM t_e);
DELETE FROM dining_meal_order WHERE elder_id IN (SELECT id FROM t_e);
DELETE FROM elder_account WHERE elder_id IN (SELECT id FROM t_e);
DELETE FROM elder_account_log WHERE elder_id IN (SELECT id FROM t_e);
DELETE FROM elder_admission WHERE elder_id IN (SELECT id FROM t_e);
DELETE FROM elder_admission_progress WHERE elder_id IN (SELECT id FROM t_e);
DELETE FROM elder_bed_relation WHERE elder_id IN (SELECT id FROM t_e);
DELETE FROM elder_change_log WHERE elder_id IN (SELECT id FROM t_e);
DELETE FROM elder_discharge WHERE elder_id IN (SELECT id FROM t_e);
DELETE FROM elder_discharge_apply WHERE elder_id IN (SELECT id FROM t_e);
DELETE FROM elder_disease WHERE elder_id IN (SELECT id FROM t_e);
DELETE FROM elder_family WHERE elder_id IN (SELECT id FROM t_e);
DELETE FROM elder_file_archive WHERE elder_id IN (SELECT id FROM t_e);
DELETE FROM elder_lifecycle_event WHERE elder_id IN (SELECT id FROM t_e);
DELETE FROM finance_admission_fee_audit WHERE elder_id IN (SELECT id FROM t_e);
DELETE FROM finance_consumption_record WHERE elder_id IN (SELECT id FROM t_e);
DELETE FROM finance_deposit_account WHERE elder_id IN (SELECT id FROM t_e);
DELETE FROM finance_deposit_transaction WHERE elder_id IN (SELECT id FROM t_e);
DELETE FROM finance_discharge_fee_audit WHERE elder_id IN (SELECT id FROM t_e);
DELETE FROM finance_discharge_settlement WHERE elder_id IN (SELECT id FROM t_e);
DELETE FROM finance_reminder WHERE elder_id IN (SELECT id FROM t_e);
DELETE FROM health_archive WHERE elder_id IN (SELECT id FROM t_e);
DELETE FROM health_basic_record WHERE elder_id IN (SELECT id FROM t_e);
DELETE FROM health_data_record WHERE elder_id IN (SELECT id FROM t_e);
DELETE FROM health_nursing_log WHERE elder_id IN (SELECT id FROM t_e);
DELETE FROM incident_report WHERE elder_id IN (SELECT id FROM t_e);
DELETE FROM medical_order WHERE elder_id IN (SELECT id FROM t_e);
DELETE FROM points_log WHERE elder_id IN (SELECT id FROM t_e);
DELETE FROM smart_alert WHERE elder_id IN (SELECT id FROM t_e);
DELETE FROM vital_sign_record WHERE elder_id IN (SELECT id FROM t_e);
DELETE FROM elder_points_account WHERE elder_id IN (SELECT id FROM t_e);
DELETE FROM one_card_account WHERE elder_id IN (SELECT id FROM t_e);
DELETE FROM crm_contract_workflow_log WHERE contract_id IN (SELECT id FROM t_c) OR lead_id IN (SELECT id FROM t_l);
DELETE FROM crm_stage_transition_log WHERE contract_id IN (SELECT id FROM t_c) OR lead_id IN (SELECT id FROM t_l);
DELETE FROM crm_contract_attachment WHERE contract_id IN (SELECT id FROM t_c);
DELETE FROM crm_sms_task WHERE contract_id IN (SELECT id FROM t_c) OR lead_id IN (SELECT id FROM t_l);
DELETE FROM crm_contract WHERE id IN (SELECT id FROM t_c);
DELETE FROM crm_visit_appointment WHERE lead_id IN (SELECT id FROM t_l);
DELETE FROM crm_callback_plan WHERE lead_id IN (SELECT id FROM t_l);
DELETE FROM crm_lead_assign_log WHERE lead_id IN (SELECT id FROM t_l);
DELETE FROM crm_lead WHERE id IN (SELECT id FROM t_l);
DELETE FROM elder WHERE id IN (SELECT id FROM t_e);
DELETE FROM family_user WHERE id IN (SELECT id FROM t_f);
DELETE FROM billing_config WHERE remark = '流程测试临时';
DELETE FROM care_level WHERE remark = '流程测试临时';

-- 人事线
CREATE TEMPORARY TABLE t_st AS SELECT id FROM staff WHERE username LIKE 'flow%' AND real_name LIKE '流程测试%';
CREATE TEMPORARY TABLE t_pb AS SELECT id FROM hr_payroll_batch WHERE period = '2099-01';
DELETE FROM hr_payroll_line WHERE batch_id IN (SELECT id FROM t_pb);
DELETE FROM hr_payroll_batch WHERE id IN (SELECT id FROM t_pb);
DELETE FROM hr_performance_appeal WHERE staff_id IN (SELECT id FROM t_st);
DELETE FROM staff_points_log WHERE staff_id IN (SELECT id FROM t_st);
DELETE FROM staff_points_account WHERE staff_id IN (SELECT id FROM t_st);
DELETE FROM staff_profile WHERE staff_id IN (SELECT id FROM t_st);
DELETE FROM staff_role WHERE staff_id IN (SELECT id FROM t_st);
DELETE FROM staff WHERE id IN (SELECT id FROM t_st);
DELETE FROM oa_approval WHERE title LIKE '流程测试%';
DELETE FROM oa_task WHERE title LIKE '流程测试%';
DELETE FROM oa_notice_read WHERE notice_id IN (SELECT id FROM oa_notice WHERE title LIKE '流程测试%');
DELETE FROM oa_notice WHERE title LIKE '流程测试%';
DELETE FROM oa_todo WHERE title LIKE '%流程测试%' OR content LIKE '%流程测试%';
DELETE FROM health_inspection WHERE elder_name LIKE '流程测试%';

-- 后勤线
CREATE TEMPORARY TABLE t_p AS SELECT id FROM product WHERE product_name LIKE '流程测试%';
CREATE TEMPORARY TABLE t_w AS SELECT id FROM material_warehouse WHERE warehouse_name LIKE '流程测试%';
CREATE TEMPORARY TABLE t_os AS SELECT DISTINCT sheet_id id FROM inventory_outbound_sheet_item WHERE product_id IN (SELECT id FROM t_p);
DELETE FROM inventory_outbound_sheet_item WHERE sheet_id IN (SELECT id FROM t_os);
DELETE FROM inventory_outbound_sheet WHERE id IN (SELECT id FROM t_os);
DELETE FROM inventory_adjustment WHERE product_id IN (SELECT id FROM t_p);
DELETE FROM inventory_log WHERE product_id IN (SELECT id FROM t_p);
DELETE FROM inventory_batch WHERE product_id IN (SELECT id FROM t_p);
DELETE FROM material_purchase_order WHERE id IN (SELECT order_id FROM material_purchase_order_item WHERE product_id IN (SELECT id FROM t_p));
DELETE FROM material_purchase_order_item WHERE product_id IN (SELECT id FROM t_p);
DELETE FROM material_warehouse WHERE id IN (SELECT id FROM t_w);
DELETE FROM material_supplier WHERE supplier_name LIKE '流程测试%';
DELETE FROM product WHERE id IN (SELECT id FROM t_p);
DELETE FROM life_maintenance_request WHERE description LIKE '流程测试%' OR description LIKE '%流程测试%';
DELETE FROM logistics_equipment_archive WHERE equipment_name LIKE '流程测试%';
DELETE FROM dining_meal_order WHERE dish_names LIKE '流程测试%';
DELETE FROM dining_dish WHERE dish_name LIKE '流程测试%';
DELETE FROM fire_safety_record WHERE title LIKE '流程测试%';

SELECT (SELECT count(*) FROM t_e) elders, (SELECT count(*) FROM t_l) leads, (SELECT count(*) FROM t_st) staff,
       (SELECT count(*) FROM t_pb) payroll_batches, (SELECT count(*) FROM t_p) products;
COMMIT;
