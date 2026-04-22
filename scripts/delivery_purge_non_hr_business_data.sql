-- 交付清库：
-- 保留人力资源/系统管理相关基础数据（org/department/role/staff/staff_role/base_data_item/staff_*/attendance_*/hr_*），
-- 清空其余业务域测试数据，便于交付演示/验收环境。
-- 用法示例：
-- docker compose exec -T mysql mysql -uroot -proot zhiyangyun < scripts/delivery_purge_non_hr_business_data.sql

SET FOREIGN_KEY_CHECKS = 0;
SET SQL_SAFE_UPDATES = 0;

-- CRM / 市场
DELETE FROM crm_marketing_plan_department;
DELETE FROM crm_marketing_plan_approval;
DELETE FROM crm_marketing_plan_receipt;
DELETE FROM crm_marketing_plan_performance;
DELETE FROM crm_lead_assign_log;
DELETE FROM crm_stage_transition_log;
DELETE FROM crm_contract_workflow_log;
DELETE FROM crm_sales_report_snapshot;
DELETE FROM crm_callback_plan;
DELETE FROM crm_contract_attachment;
DELETE FROM crm_sms_task;
DELETE FROM crm_contract;
DELETE FROM crm_marketing_plan;
DELETE FROM crm_lead;

-- 长者 / 家属 / 入住
DELETE FROM elder_family;
DELETE FROM family_notify_log;
DELETE FROM family_sms_code_log;
DELETE FROM family_recharge_order;
DELETE FROM family_portal_state;
DELETE FROM family_user;
DELETE FROM elder_bed_relation;
DELETE FROM elder_lifecycle_event;
DELETE FROM elder_outing_record;
DELETE FROM elder_trial_stay;
DELETE FROM elder_discharge_apply;
DELETE FROM elder_medical_outing_record;
DELETE FROM elder_death_register;
DELETE FROM elder_admission;
DELETE FROM elder_change_log;
DELETE FROM elder_discharge;
DELETE FROM elder_account_log;
DELETE FROM elder_account;
DELETE FROM elder_disease;
DELETE FROM elder_points_account;
DELETE FROM points_log;
DELETE FROM elder_package;
DELETE FROM elder;

-- 床位 / 房间 / 资产布局
DELETE FROM care_task_execute_log;
DELETE FROM care_task_review;
DELETE FROM care_task_daily;
DELETE FROM care_task_template;
DELETE FROM visit_qrcode_check_log;
DELETE FROM visit_booking;
DELETE FROM bed;
DELETE FROM room;
DELETE FROM floor;
DELETE FROM building;

-- 护理 / 服务
DELETE FROM caregiver_group_member;
DELETE FROM caregiver_group;
DELETE FROM shift_swap_request;
DELETE FROM shift_handover;
DELETE FROM shift_template;
DELETE FROM service_booking;
DELETE FROM service_plan;
DELETE FROM care_package_item;
DELETE FROM care_package;
DELETE FROM care_level;
DELETE FROM service_item;

-- 健康 / 医疗 / 评估
DELETE FROM assessment_reminder_log;
DELETE FROM assessment_record;
DELETE FROM assessment_scale_template;
DELETE FROM medical_tcm_assessment;
DELETE FROM medical_cvd_risk_assessment;
DELETE FROM medical_alert_rule_config;
DELETE FROM health_medication_task;
DELETE FROM health_nursing_log;
DELETE FROM health_inspection;
DELETE FROM health_data_record;
DELETE FROM health_archive;
DELETE FROM health_medication_registration;
DELETE FROM health_medication_setting;
DELETE FROM health_medication_deposit;
DELETE FROM health_drug_dictionary;
DELETE FROM health_basic_record;
DELETE FROM vital_sign_record;
DELETE FROM vital_threshold_config;

-- 餐饮 / 生活 / 一卡通
DELETE FROM dining_risk_override;
DELETE FROM dining_risk_intercept_log;
DELETE FROM dining_risk_threshold_config;
DELETE FROM dining_delivery_record;
DELETE FROM dining_meal_order;
DELETE FROM dining_recipe;
DELETE FROM dining_dish;
DELETE FROM dining_prep_zone;
DELETE FROM dining_delivery_area;
DELETE FROM meal_plan;
DELETE FROM activity_event;
DELETE FROM incident_report;
DELETE FROM one_card_account;
DELETE FROM life_room_cleaning_task;
DELETE FROM life_maintenance_request;

-- 财务 / 账单
DELETE FROM finance_refund_voucher;
DELETE FROM finance_monthly_allocation;
DELETE FROM finance_consumption_record;
DELETE FROM finance_discharge_settlement;
DELETE FROM finance_discharge_fee_audit;
DELETE FROM finance_admission_fee_audit;
DELETE FROM payment_record;
DELETE FROM reconciliation_daily;
DELETE FROM bill_item;
DELETE FROM bill_monthly;
DELETE FROM billing_care_level_fee;
DELETE FROM billing_config;

-- 物资 / 库存 / 商品
DELETE FROM inventory_outbound_sheet_item;
DELETE FROM inventory_outbound_sheet;
DELETE FROM inventory_log;
DELETE FROM inventory_adjustment;
DELETE FROM inventory_batch;
DELETE FROM material_transfer_item;
DELETE FROM material_transfer_order;
DELETE FROM material_purchase_order_item;
DELETE FROM material_purchase_order;
DELETE FROM material_supplier;
DELETE FROM material_warehouse;
DELETE FROM order_item;
DELETE FROM `order`;
DELETE FROM product;
DELETE FROM product_category;
DELETE FROM product_tag;
DELETE FROM disease_forbidden_tag_audit;
DELETE FROM disease_forbidden_tag;
DELETE FROM disease;

-- 后勤 / 消防 / 巡检
DELETE FROM logistics_maintenance_todo_job_log;
DELETE FROM logistics_equipment_archive;
DELETE FROM fire_safety_record;

-- OA / 调查 / 内容
DELETE FROM oa_quick_chat_state;
DELETE FROM oa_document_folder;
DELETE FROM oa_suggestion;
DELETE FROM oa_work_report;
DELETE FROM oa_album;
DELETE FROM oa_knowledge;
DELETE FROM oa_notice;
DELETE FROM oa_todo;
DELETE FROM oa_approval;
DELETE FROM oa_document;
DELETE FROM oa_task;
DELETE FROM oa_group_setting;
DELETE FROM oa_activity_plan;
DELETE FROM report_record;
DELETE FROM survey_submission_item;
DELETE FROM survey_submission;
DELETE FROM survey_template_question;
DELETE FROM survey_template;
DELETE FROM survey_question_bank;

-- 审计日志
DELETE FROM audit_log;

SET SQL_SAFE_UPDATES = 1;
SET FOREIGN_KEY_CHECKS = 1;
