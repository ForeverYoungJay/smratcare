SET NAMES utf8mb4;
SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_results = utf8mb4;

SET FOREIGN_KEY_CHECKS=0;

TRUNCATE TABLE crm_sms_task;
TRUNCATE TABLE crm_contract_attachment;
TRUNCATE TABLE crm_callback_plan;
TRUNCATE TABLE assessment_record;
TRUNCATE TABLE elder_admission;
TRUNCATE TABLE elder_bed_relation;
TRUNCATE TABLE crm_lead;
TRUNCATE TABLE elder;
TRUNCATE TABLE bed;
TRUNCATE TABLE room;
TRUNCATE TABLE staff_role;
TRUNCATE TABLE staff;
TRUNCATE TABLE role;
TRUNCATE TABLE department;
TRUNCATE TABLE org;

SET FOREIGN_KEY_CHECKS=1;

INSERT INTO org (id, org_code, org_name, org_type, status, contact_name, contact_phone, address)
VALUES (1, 'ORG001', '智养云测试机构', '养老院', 1, '测试负责人', '13800000000', '上海市静安区测试路 88 号');

INSERT INTO department (id, org_id, parent_id, dept_name, dept_code, sort_no, status)
VALUES
  (101, 1, NULL, '行政部', 'ADMIN_DEPT', 1, 1),
  (102, 1, NULL, '营销部', 'MARKETING_DEPT', 2, 1),
  (103, 1, NULL, '评估部', 'ASSESSMENT_DEPT', 3, 1);

INSERT INTO role (id, org_id, role_name, role_code, role_desc, status)
VALUES
  (201, 1, '系统管理员', 'ADMIN', '管理后台全权限', 1),
  (202, 1, '普通员工', 'STAFF', '普通业务人员', 1);

-- 统一密码: 123456
INSERT INTO staff (id, org_id, department_id, staff_no, username, password_hash, real_name, phone, email, gender, status)
VALUES
  (301, 1, 101, 'S0001', 'admin', '$2a$10$oHe4Uo7TeO5Ftwj0zK33auA8wXIMcbHScVrPTYbeTnrrrkbEJX.Qe', '系统管理员', '13900000001', 'admin@demo.com', 1, 1),
  (302, 1, 102, 'S0002', 'marketer', '$2a$10$oHe4Uo7TeO5Ftwj0zK33auA8wXIMcbHScVrPTYbeTnrrrkbEJX.Qe', '营销专员', '13900000002', 'marketer@demo.com', 1, 1),
  (303, 1, 103, 'S0003', 'assessor', '$2a$10$oHe4Uo7TeO5Ftwj0zK33auA8wXIMcbHScVrPTYbeTnrrrkbEJX.Qe', '评估专员', '13900000003', 'assessor@demo.com', 2, 1);

INSERT INTO staff_role (id, org_id, staff_id, role_id)
VALUES
  (401, 1, 301, 201),
  (402, 1, 302, 202),
  (403, 1, 303, 202);

INSERT INTO room (id, tenant_id, org_id, room_no, room_type, capacity, status, room_qr_code, created_by)
VALUES
  (1001, 1, 1, 'A101', '双人间', 2, 1, 'ROOM_A101', 301),
  (1002, 1, 1, 'A102', '单人间', 1, 1, 'ROOM_A102', 301);

INSERT INTO bed (id, tenant_id, org_id, room_id, bed_no, bed_qr_code, status, elder_id, created_by)
VALUES
  (1101, 1, 1, 1001, '01', 'BED_A101_01', 2, 2001, 301),
  (1102, 1, 1, 1001, '02', 'BED_A101_02', 2, 2002, 301),
  (1103, 1, 1, 1002, '01', 'BED_A102_01', 1, NULL, 301);

INSERT INTO elder (id, tenant_id, org_id, elder_code, elder_qr_code, full_name, id_card_no, gender, birth_date, phone, home_address, admission_date, status, bed_id, care_level, remark, created_by)
VALUES
  (2001, 1, 1, 'ELD001', 'EQR001', '张三', '310101194901010011', 1, '1949-01-01', '13600000001', '上海市黄浦区人民路 1 号', '2026-02-20', 1, 1101, 'A', '待完成入住评估', 301),
  (2002, 1, 1, 'ELD002', 'EQR002', '李四', '310101195203030022', 1, '1952-03-03', '13600000002', '上海市徐汇区中山路 2 号', '2026-02-18', 1, 1102, 'B', '已完成入住评估，待选床办理', 301),
  (2003, 1, 1, 'ELD003', 'EQR003', '王五', '310101195505050033', 2, '1955-05-05', '13600000003', '上海市浦东新区世纪大道 3 号', '2026-02-10', 1, NULL, 'B', '合同已签署', 301);

INSERT INTO elder_bed_relation (id, tenant_id, org_id, elder_id, bed_id, start_date, active_flag, created_by)
VALUES
  (2101, 1, 1, 2001, 1101, '2026-02-20', 1, 301),
  (2102, 1, 1, 2002, 1102, '2026-02-18', 1, 301);

INSERT INTO crm_lead (
  id, tenant_id, org_id, name, phone, source, customer_tag, status,
  contract_signed_flag, contract_signed_at, contract_no, consultant_name, consultant_phone,
  elder_name, elder_phone, gender, age, consult_date, consult_type, media_channel, info_source,
  receptionist_name, home_address, marketer_name, followup_status, id_card_no,
  reservation_room_no, refunded, reservation_channel, reservation_status,
  org_name, contract_status, flow_stage, current_owner_dept, contract_expiry_date,
  sms_send_count, created_by, is_deleted
)
VALUES
  (3001, 1, 1, '张三家属', '13600001001', '线上投放', '重点客户', 2,
   0, NULL, 'gfyy20260228001', '顾问A', '13911110001',
   '张三', '13600000001', 1, 77, '2026-02-22', '电话咨询', '抖音', '线上推广',
   '前台1', '上海市黄浦区人民路 1 号', '营销专员', '未回访', '310101194901010011',
   'A101-01', 0, '线上', '已交订金',
   '智养云测试机构', '待评估', 'PENDING_ASSESSMENT', 'ASSESSMENT', '2027-02-28',
   1, 301, 0),

  (3002, 1, 1, '李四家属', '13600001002', '转介绍', '潜力客户', 2,
   0, NULL, 'gfyy20260228002', '顾问B', '13911110002',
   '李四', '13600000002', 1, 74, '2026-02-21', '到店咨询', '小红书', '老客推荐',
   '前台2', '上海市徐汇区中山路 2 号', '营销专员', '已回访', '310101195203030022',
   'A101-02', 0, '线下', '已锁房',
   '智养云测试机构', '待办理入住', 'PENDING_BED_SELECT', 'MARKETING', '2027-02-28',
   2, 301, 0),

  (3003, 1, 1, '王五家属', '13600001003', '公众号', '普通客户', 2,
   1, NOW(), 'gfyy20260228003', '顾问C', '13911110003',
   '王五', '13600000003', 2, 71, '2026-02-15', '电话咨询', '微信公众号', '线上推广',
   '前台3', '上海市浦东新区世纪大道 3 号', '营销专员', '已回访', '310101195505050033',
   'A102-01', 0, '线上', '已入住',
   '智养云测试机构', '已签署', 'SIGNED', 'MARKETING', '2027-02-28',
   1, 301, 0),

  (3004, 1, 1, '赵六家属', '13600001004', '百度推广', '重点客户', 2,
   0, NULL, 'gfyy20260228004', '顾问D', '13911110004',
   '赵六', '13600000004', 2, 79, '2026-02-24', '线上咨询', '百度', '搜索广告',
   '前台4', '上海市长宁区延安路 4 号', '营销专员', '未回访', '310101194707070044',
   'A102-01', 0, '线上', '待签署',
   '智养云测试机构', '待签署', 'PENDING_SIGN', 'MARKETING', '2027-02-28',
   0, 301, 0);

INSERT INTO crm_contract_attachment (id, tenant_id, org_id, lead_id, contract_no, file_name, file_url, file_type, file_size, remark, created_by)
VALUES
  (5001, 1, 1, 3001, 'gfyy20260228001', '张三病历.pdf', '/uploads/contract/zhangsan-medical.pdf', 'application/pdf', 183421, 'MEDICAL_RECORD', 301),
  (5002, 1, 1, 3001, 'gfyy20260228001', '张三医保.jpg', '/uploads/contract/zhangsan-insurance.jpg', 'image/jpeg', 94321, 'MEDICAL_INSURANCE', 301),
  (5003, 1, 1, 3002, 'gfyy20260228002', '李四合同扫描件.pdf', '/uploads/contract/lisi-contract.pdf', 'application/pdf', 206512, 'CONTRACT', 301);

INSERT INTO crm_sms_task (id, tenant_id, org_id, lead_id, phone, template_name, content, plan_send_time, status, send_time, result_message, created_by)
VALUES
  (5101, 1, 1, 3001, '13600000001', '合同到期提醒', '您好，合同评估待处理，请尽快办理。', NOW(), 'SENT', NOW(), '发送成功', 301),
  (5102, 1, 1, 3002, '13600000002', '入住办理提醒', '您好，请携带资料办理入住。', DATE_ADD(NOW(), INTERVAL 1 DAY), 'SCHEDULED', NULL, NULL, 301);

INSERT INTO crm_callback_plan (id, tenant_id, org_id, lead_id, title, plan_execute_time, executor_name, status, created_by)
VALUES
  (5201, 1, 1, 3001, '评估回访', DATE_ADD(NOW(), INTERVAL 1 DAY), '营销专员', 'PENDING', 301),
  (5202, 1, 1, 3004, '签署催办', DATE_ADD(NOW(), INTERVAL 2 DAY), '营销专员', 'PENDING', 301);

INSERT INTO elder_admission (id, tenant_id, org_id, elder_id, admission_date, contract_no, deposit_amount, remark, created_by)
VALUES
  (6001, 1, 1, 2002, '2026-02-18', 'gfyy20260228002', 5000.00, '待最终签署后确认床位', 301),
  (6002, 1, 1, 2003, '2026-02-10', 'gfyy20260228003', 5000.00, '已正式入住', 301);

INSERT INTO assessment_record (
  id, tenant_id, org_id, elder_id, elder_name, assessment_type, template_id, level_code, score,
  assessment_date, next_assessment_date, assessor_id, assessor_name, status, result_summary, suggestion,
  detail_json, score_auto, archive_no, source, created_by, is_deleted
)
VALUES
  (7001, 1, 1, 2001, '张三', 'ADMISSION', NULL, '2级', 58.00,
   '2026-02-27', '2026-03-27', 303, '评估专员', 'DRAFT', '初评完成，需复核跌倒风险。', '建议先中度护理套餐，72小时复评。',
   '{"mobility":2,"cognitive":3}', 1, 'AR20260228001', 'resident360', 303, 0),

  (7002, 1, 1, 2002, '李四', 'ADMISSION', NULL, '1级', 72.00,
   '2026-02-20', '2026-03-20', 303, '评估专员', 'COMPLETED', '可完成多数日常活动，需轻度协助。', '建议轻度护理套餐，关注夜间排泄安全。',
   '{"mobility":3,"cognitive":4}', 1, 'AR20260228002', 'resident360', 303, 0),

  (7003, 1, 1, 2003, '王五', 'ADMISSION', NULL, '1级', 76.00,
   '2026-02-11', '2026-03-11', 303, '评估专员', 'COMPLETED', '评估通过，可进入稳定在院服务。', '建议按月复评，维持现有护理频次。',
   '{"mobility":4,"cognitive":4}', 1, 'AR20260228003', 'resident360', 303, 0);
