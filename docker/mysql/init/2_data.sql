SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS=0;

-- org / department
INSERT INTO org (id, org_code, org_name, org_type, status, contact_name, contact_phone, address, create_time, update_time, is_deleted) VALUES
(1, 'ZYY-001', '智养云示例机构', '养老机构', 1, '管理员', '13000000000', '北京市朝阳区示例路1号', '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0);

INSERT INTO department (id, org_id, parent_id, dept_name, dept_code, sort_no, status, create_time, update_time, is_deleted) VALUES
(10, 1, NULL, '护理部', 'CARE', 1, 1, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(11, 1, NULL, '医务部', 'MED', 2, 1, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(12, 1, NULL, '财务部', 'FIN', 3, 1, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(13, 1, NULL, '门卫室', 'GUARD', 4, 1, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0);

-- role / staff
INSERT INTO role (id, org_id, role_name, role_code, role_desc, status, create_time, update_time, is_deleted) VALUES
(100, 1, '管理员', 'ADMIN', '系统管理员', 1, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(101, 1, '护士长', 'NURSE_MANAGER', '护理管理', 1, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(102, 1, '护工', 'CARE_WORKER', '护理执行', 1, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(103, 1, '医生', 'DOCTOR', '医生', 1, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(104, 1, '财务', 'FINANCE', '财务', 1, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(105, 1, '门卫', 'GUARD', '门卫', 1, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0);

-- password: 123456 (bcrypt $2a$)
INSERT INTO staff (id, org_id, department_id, staff_no, username, password_hash, real_name, phone, email, gender, status, last_login_time, create_time, update_time, is_deleted) VALUES
(500, 1, 10, 'S0001', 'admin', '$2a$10$A1rIAhc8BXcMXR/imDcBMeabjSM2/2z4pIjpGYBnMJ8Z.C8Z1e/Ym', 'Admin', '13000000000', NULL, 1, 1, NULL, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(501, 1, 10, 'S0002', 'nurse', '$2a$10$A1rIAhc8BXcMXR/imDcBMeabjSM2/2z4pIjpGYBnMJ8Z.C8Z1e/Ym', '护士长王', '13000000001', NULL, 2, 1, NULL, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(502, 1, 10, 'S0003', 'worker1', '$2a$10$A1rIAhc8BXcMXR/imDcBMeabjSM2/2z4pIjpGYBnMJ8Z.C8Z1e/Ym', '护工李', '13000000002', NULL, 1, 1, NULL, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(503, 1, 11, 'S0004', 'doctor', '$2a$10$A1rIAhc8BXcMXR/imDcBMeabjSM2/2z4pIjpGYBnMJ8Z.C8Z1e/Ym', '医生张', '13000000003', NULL, 1, 1, NULL, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(504, 1, 12, 'S0005', 'finance', '$2a$10$A1rIAhc8BXcMXR/imDcBMeabjSM2/2z4pIjpGYBnMJ8Z.C8Z1e/Ym', '财务刘', '13000000004', NULL, 2, 1, NULL, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(505, 1, 13, 'S0006', 'guard', '$2a$10$A1rIAhc8BXcMXR/imDcBMeabjSM2/2z4pIjpGYBnMJ8Z.C8Z1e/Ym', '门卫赵', '13000000005', NULL, 1, 1, NULL, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0);

INSERT INTO staff_role (id, org_id, staff_id, role_id, create_time, update_time, is_deleted) VALUES
(600, 1, 500, 100, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(601, 1, 501, 101, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(602, 1, 502, 102, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(603, 1, 503, 103, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(604, 1, 504, 104, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(605, 1, 505, 105, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0);

-- room / bed
INSERT INTO room (id, org_id, building, floor_no, room_no, room_type, capacity, status, create_time, update_time, is_deleted) VALUES
(1001, 1, 'A栋', '1F', 'A101', '双人间', 2, 1, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(1002, 1, 'A栋', '1F', 'A102', '双人间', 2, 1, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(1003, 1, 'B栋', '2F', 'B201', '三人间', 3, 1, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0);

INSERT INTO bed (id, org_id, room_id, bed_no, bed_qr_code, status, elder_id, create_time, update_time, is_deleted) VALUES
(2001, 1, 1001, 'A101-1', 'BED-A101-1-QR', 2, 4001, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(2002, 1, 1001, 'A101-2', 'BED-A101-2-QR', 1, NULL, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(2003, 1, 1002, 'A102-1', 'BED-A102-1-QR', 2, 4002, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(2004, 1, 1002, 'A102-2', 'BED-A102-2-QR', 1, NULL, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(2005, 1, 1003, 'B201-1', 'BED-B201-1-QR', 2, 4003, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(2006, 1, 1003, 'B201-2', 'BED-B201-2-QR', 1, NULL, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(2007, 1, 1003, 'B201-3', 'BED-B201-3-QR', 1, NULL, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0);

-- elder / family
INSERT INTO elder (id, org_id, elder_code, elder_qr_code, full_name, id_card_no, gender, birth_date, phone, admission_date, status, bed_id, care_level, remark, create_time, update_time, is_deleted) VALUES
(4001, 1, 'E1001', 'ELDER-QR-4001', '王大爷', '110101194001010011', 1, '1940-01-01', '13800000001', '2025-12-15', 1, 2001, 'A', '高血压', '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(4002, 1, 'E1002', 'ELDER-QR-4002', '李奶奶', '110101194502020022', 2, '1945-02-02', '13800000002', '2026-01-10', 1, 2003, 'B', '糖尿病', '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(4003, 1, 'E1003', 'ELDER-QR-4003', '张爷爷', '110101193803030033', 1, '1938-03-03', '13800000003', '2026-02-01', 1, 2005, 'C', '痛风', '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0);

INSERT INTO elder_bed_relation (id, org_id, elder_id, bed_id, start_date, end_date, active_flag, remark, create_time, update_time, is_deleted) VALUES
(5001, 1, 4001, 2001, '2025-12-15', NULL, 1, NULL, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(5002, 1, 4002, 2003, '2026-01-10', NULL, 1, NULL, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(5003, 1, 4003, 2005, '2026-02-01', NULL, 1, NULL, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0);

INSERT INTO family_user (id, org_id, open_id, username, password_hash, real_name, phone, id_card_no, status, create_time, update_time, is_deleted) VALUES
(7001, 1, NULL, NULL, NULL, '王小明', '13900000001', NULL, 1, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(7002, 1, NULL, NULL, NULL, '李小红', '13900000002', NULL, 1, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0);

INSERT INTO elder_family (id, org_id, elder_id, family_user_id, relation, is_primary, remark, create_time, update_time, is_deleted) VALUES
(8001, 1, 4001, 7001, '儿子', 1, NULL, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(8002, 1, 4002, 7002, '女儿', 1, NULL, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0);

-- disease / tag / forbidden
INSERT INTO disease (id, org_id, disease_code, disease_name, remark, create_time, update_time, is_deleted) VALUES
(9001, 1, 'DIABETES', '糖尿病', NULL, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(9002, 1, 'GOUT', '痛风', NULL, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(9003, 1, 'HYPERTENSION', '高血压', NULL, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0);

INSERT INTO elder_disease (id, org_id, elder_id, disease_id, create_time, update_time, is_deleted) VALUES
(9101, 1, 4002, 9001, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(9102, 1, 4003, 9002, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(9103, 1, 4001, 9003, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0);

INSERT INTO product_tag (id, org_id, tag_name, tag_code, tag_type, status, create_time, update_time, is_deleted) VALUES
(9201, 1, '含糖', 'SUGAR', 'FOOD', 1, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(9202, 1, '海鲜', 'SEAFOOD', 'FOOD', 1, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(9203, 1, '高盐', 'HIGH_SALT', 'FOOD', 1, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(9204, 1, '低糖', 'LOW_SUGAR', 'FOOD', 1, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0);

INSERT INTO disease_forbidden_tag (id, org_id, disease_id, tag_id, create_time, update_time, is_deleted) VALUES
(9301, 1, 9001, 9201, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(9302, 1, 9002, 9202, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(9303, 1, 9003, 9203, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0);

-- product / inventory
INSERT INTO product (id, org_id, product_code, product_name, category, unit, price, points_price, safety_stock, status, tag_ids, create_time, update_time, is_deleted) VALUES
(6001, 1, 'SUGAR_500G', '白砂糖', '食品', '袋', 10.00, 10, 50, 1, '9201', '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(6002, 1, 'SEAFOOD_BOX', '海鲜礼包', '食品', '盒', 30.00, 30, 20, 1, '9202', '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(6003, 1, 'MILK_250ML', '纯牛奶', '饮品', '瓶', 5.00, 5, 80, 1, '', '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(6004, 1, 'LOW_SUGAR_BIS', '低糖饼干', '食品', '盒', 8.00, 8, 40, 1, '9204', '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0);

INSERT INTO inventory_batch (id, org_id, product_id, batch_no, quantity, cost_price, expire_date, warehouse_location, create_time, update_time, is_deleted) VALUES
(6101, 1, 6001, 'BATCH-6001-202602', 300, 3.50, '2026-08-01', 'A-01', '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(6102, 1, 6002, 'BATCH-6002-202602', 120, 12.00, '2026-06-15', 'B-02', '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(6103, 1, 6003, 'BATCH-6003-202602', 500, 2.00, '2026-05-10', 'A-02', '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(6104, 1, 6004, 'BATCH-6004-202602', 200, 3.00, '2026-07-20', 'A-03', '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0);

INSERT INTO inventory_log (id, org_id, product_id, batch_id, change_type, change_qty, ref_order_id, ref_adjustment_id, remark, create_time, update_time, is_deleted) VALUES
(6201, 1, 6001, 6101, 'IN', 300, NULL, NULL, '初始化入库', '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(6202, 1, 6002, 6102, 'IN', 120, NULL, NULL, '初始化入库', '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(6203, 1, 6003, 6103, 'IN', 500, NULL, NULL, '初始化入库', '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(6204, 1, 6004, 6104, 'IN', 200, NULL, NULL, '初始化入库', '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0);

INSERT INTO elder_points_account 
(id, org_id, elder_id, points_balance, create_time, update_time, is_deleted) 
VALUES
(6301, 1, 4001, 200, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(6302, 1, 4002, 100, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(6303, 1, 4003, 80, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0);


-- orders
INSERT INTO `order` (id, org_id, order_no, elder_id, total_amount, points_used, payable_amount, pay_status, pay_time, order_status, create_time, update_time, is_deleted) VALUES
(91001, 1, 'ORD20260208-PAID-001', 4002, 10.00, 10, 10.00, 1, '2026-02-08 10:00:00', 2, '2026-02-08 10:00:00', '2026-02-08 10:00:00', 0),
(91002, 1, 'ORD20260208-PAID-002', 4003, 30.00, 30, 30.00, 1, '2026-02-08 10:30:00', 2, '2026-02-08 10:30:00', '2026-02-08 10:30:00', 0),
(91003, 1, 'ORD20260208-PAID-003', 4001, 5.00, 5, 5.00, 1, '2026-02-08 11:00:00', 2, '2026-02-08 11:00:00', '2026-02-08 11:00:00', 0);

INSERT INTO order_item (id, org_id, order_id, product_id, product_name, unit_price, quantity, total_amount, create_time, update_time, is_deleted) VALUES
(92001, 1, 91001, 6001, '白砂糖', 10.00, 1, 10.00, '2026-02-08 10:00:00', '2026-02-08 10:00:00', 0),
(92002, 1, 91002, 6002, '海鲜礼包', 30.00, 1, 30.00, '2026-02-08 10:30:00', '2026-02-08 10:30:00', 0),
(92003, 1, 91003, 6003, '纯牛奶', 5.00, 1, 5.00, '2026-02-08 11:00:00', '2026-02-08 11:00:00', 0);

INSERT INTO points_log 
(id, org_id, elder_id, change_type, change_points, balance_after, ref_order_id, remark, create_time, update_time, is_deleted) 
VALUES
(94001, 1, 4002, 'DEDUCT', -10, 90, 91001, '商城消费', '2026-02-08 10:00:00', '2026-02-08 10:00:00', 0),
(94002, 1, 4003, 'DEDUCT', -30, 50, 91002, '商城消费', '2026-02-08 10:30:00', '2026-02-08 10:30:00', 0),
(94003, 1, 4001, 'DEDUCT', -5, 195, 91003, '商城消费', '2026-02-08 11:00:00', '2026-02-08 11:00:00', 0);


-- inventory out log for orders (simulate fifo)
INSERT INTO inventory_log (id, org_id, product_id, batch_id, change_type, change_qty, ref_order_id, ref_adjustment_id, remark, create_time, update_time, is_deleted) VALUES
(63001, 1, 6001, 6101, 'OUT', -1, 91001, NULL, '订单出库', '2026-02-08 10:00:00', '2026-02-08 10:00:00', 0),
(63002, 1, 6002, 6102, 'OUT', -1, 91002, NULL, '订单出库', '2026-02-08 10:30:00', '2026-02-08 10:30:00', 0),
(63003, 1, 6003, 6103, 'OUT', -1, 91003, NULL, '订单出库', '2026-02-08 11:00:00', '2026-02-08 11:00:00', 0);

-- care task template / daily
INSERT INTO care_task_template 
(id, org_id, task_name, frequency_per_day, care_level_required, enabled, create_time, update_time, is_deleted) 
VALUES
(70001, 1, '测体温', 1, 'A', 1, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(70002, 1, '量血压', 1, 'B', 1, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(70003, 1, '整理床铺', 1, 'C', 1, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0);


INSERT INTO care_task_daily
(id, org_id, template_id, elder_id, bed_id, assigned_staff_id, task_date, plan_time, status, create_time, update_time, is_deleted)
VALUES
(71001, 1, 70001, 4001, 2001, 502, '2026-02-08', '2026-02-08 09:00:00', 'PENDING', '2026-02-08 08:05:00', '2026-02-08 08:05:00', 0),
(71002, 1, 70002, 4002, 2003, 502, '2026-02-08', '2026-02-08 09:30:00', 'DONE', '2026-02-08 08:05:00', '2026-02-08 10:00:00', 0),
(71003, 1, 70003, 4003, 2005, 502, '2026-02-08', '2026-02-08 10:00:00', 'EXCEPTION', '2026-02-08 08:05:00', '2026-02-08 10:30:00', 0);

INSERT INTO care_task_execute_log 
(id, org_id, task_daily_id, elder_id, bed_id, staff_id, execute_time, bed_qr_code, result_status, suspicious_flag, remark, create_time, update_time, is_deleted) 
VALUES
(72001, 1, 71002, 4002, 2003, 502, '2026-02-08 10:00:00', 'BED-A102-1-QR', 1, 0, '血压正常', '2026-02-08 10:00:00', '2026-02-08 10:00:00', 0),
(72002, 1, 71003, 4003, 2005, 502, '2026-02-08 10:30:00', 'BED-B201-1-QR', 0, 1, '老人拒绝', '2026-02-08 10:30:00', '2026-02-08 10:30:00', 0);

-- vital
INSERT INTO vital_threshold_config 
(id, org_id, type, metric_code, min_value, max_value, status, remark, create_time, update_time, is_deleted) 
VALUES
(80001, 1, 'BP', 'SBP', 90, 140, 1, NULL, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(80002, 1, 'BP', 'DBP', 60, 90, 1, NULL, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(80003, 1, 'TEMP', 'TEMP', 36.0, 37.5, 1, NULL, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(80004, 1, 'BS', 'BS', 3.9, 7.8, 1, NULL, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0);

INSERT INTO vital_sign_record 
(id, org_id, elder_id, type, value_json, measured_at, recorded_by_staff_id, abnormal_flag, remark, create_time, update_time, is_deleted) 
VALUES
(81001, 1, 4001, 'TEMP', '{"temp":36.7}', '2026-02-08 09:10:00', 502, 0, NULL, '2026-02-08 09:10:00', '2026-02-08 09:10:00', 0),
(81002, 1, 4002, 'BP', '{"sbp":150,"dbp":95}', '2026-02-08 09:40:00', 502, 1, '血压偏高', '2026-02-08 09:40:00', '2026-02-08 09:40:00', 0);

-- billing config
INSERT INTO billing_config (id, org_id, config_key, config_value, effective_month, status, remark, create_time, update_time, is_deleted) VALUES
(85001, 1, 'bed_fee', 1200.00, '2026-01', 1, '床位费/月', '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(85002, 1, 'meal_fee', 600.00, '2026-01', 1, '餐费/月', '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(85003, 1, 'meal_fee_per_day', 20.00, '2026-01', 1, '餐费/天', '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0);

INSERT INTO billing_care_level_fee (id, org_id, care_level, fee_monthly, effective_month, status, remark, create_time, update_time, is_deleted) VALUES
(85011, 1, 'A', 800.00, '2026-01', 1, NULL, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(85012, 1, 'B', 1000.00, '2026-01', 1, NULL, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0),
(85013, 1, 'C', 1200.00, '2026-01', 1, NULL, '2026-02-08 08:00:00', '2026-02-08 08:00:00', 0);

-- monthly bill
INSERT INTO bill_monthly (id, org_id, elder_id, bill_month, total_amount, paid_amount, outstanding_amount, status, create_time, update_time, is_deleted) VALUES
(88001, 1, 4001, '2026-01', 2605.00, 0.00, 2605.00, 0, '2026-02-01 00:05:00', '2026-02-01 00:05:00', 0),
(88002, 1, 4002, '2026-01', 2620.00, 1200.00, 1420.00, 1, '2026-02-01 00:05:00', '2026-02-08 11:30:00', 0);

INSERT INTO bill_item (id, org_id, bill_monthly_id, item_type, item_name, amount, remark, create_time, update_time, is_deleted) VALUES
(88101, 1, 88001, 'BED', '床位费', 1200.00, '床位费/月', '2026-02-01 00:05:00', '2026-02-01 00:05:00', 0),
(88102, 1, 88001, 'CARE', '护理费', 800.00, '护理等级A', '2026-02-01 00:05:00', '2026-02-01 00:05:00', 0),
(88103, 1, 88001, 'MEAL', '餐费', 600.00, '餐费/月', '2026-02-01 00:05:00', '2026-02-01 00:05:00', 0),
(88104, 1, 88001, 'STORE', '商城消费', 5.00, '订单消费', '2026-02-01 00:05:00', '2026-02-01 00:05:00', 0),
(88111, 1, 88002, 'BED', '床位费', 1200.00, '床位费/月', '2026-02-01 00:05:00', '2026-02-01 00:05:00', 0),
(88112, 1, 88002, 'CARE', '护理费', 1000.00, '护理等级B', '2026-02-01 00:05:00', '2026-02-01 00:05:00', 0),
(88113, 1, 88002, 'MEAL', '餐费', 600.00, '餐费/月', '2026-02-01 00:05:00', '2026-02-01 00:05:00', 0),
(88114, 1, 88002, 'STORE', '商城消费', -180.00, '订单消费抵扣/退款', '2026-02-01 00:05:00', '2026-02-01 00:05:00', 0);

INSERT INTO payment_record (id, org_id, bill_monthly_id, amount, pay_method, external_txn_id, paid_at, operator_staff_id, remark, create_time, update_time, is_deleted) VALUES
(89001, 1, 88002, 1200.00, 'CASH', 'CASH-20260208-001', '2026-02-08 11:30:00', 504, '现金收款', '2026-02-08 11:30:00', '2026-02-08 11:30:00', 0);

INSERT INTO reconciliation_daily (id, org_id, reconcile_date, total_received, mismatch_flag, remark, create_time, update_time, is_deleted) VALUES
(90001, 1, '2026-02-08', 1200.00, 0, NULL, '2026-02-08 23:00:00', '2026-02-08 23:00:00', 0);

-- visit booking
INSERT INTO visit_booking 
(id, org_id, elder_id, family_user_id, visit_date, visit_time, visit_time_slot, visitor_count, car_plate, status, verify_code, visit_code, remark, create_time, update_time, is_deleted) 
VALUES
(95001, 1, 4001, 7001, '2026-02-09', '2026-02-09 15:00:00', '15:00-16:00', 2, '京A12345', 0, 'VCODE-95001', 'VISIT-20260209-001', NULL, '2026-02-08 09:00:00', '2026-02-08 09:00:00', 0);

INSERT INTO visit_qrcode_check_log 
(id, org_id, booking_id, check_time, staff_id, qrcode_token, result_status, remark, create_time, update_time, is_deleted) 
VALUES
(96001, 1, 95001, '2026-02-09 14:55:00', 505, 'QRCODE-95001-TOKEN', 1, 'PASS', '2026-02-09 14:55:00', '2026-02-09 14:55:00', 0);

SET FOREIGN_KEY_CHECKS=1;
