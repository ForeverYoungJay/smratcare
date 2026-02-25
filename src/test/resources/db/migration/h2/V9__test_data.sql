INSERT INTO room (id, org_id, room_no) VALUES (10, 1, 'A101');
INSERT INTO room (id, org_id, room_no) VALUES (11, 1, 'A102');

INSERT INTO bed (id, org_id, room_id, bed_no, bed_qr_code, status, elder_id)
VALUES (100, 1, 10, '01', 'QR1', 2, 200);
INSERT INTO bed (id, org_id, room_id, bed_no, bed_qr_code, status, elder_id)
VALUES (101, 1, 11, '01', 'QR2', 2, 201);

INSERT INTO elder (id, org_id, elder_code, elder_qr_code, full_name, bed_id, care_level, status)
VALUES (200, 1, 'E0001', 'EQR1', 'Elder One', 100, 'A', 1);
INSERT INTO elder (id, org_id, elder_code, elder_qr_code, full_name, bed_id, care_level, status)
VALUES (201, 1, 'E0002', 'EQR2', 'Elder Two', 101, 'A', 1);

INSERT INTO org (id, org_code, org_name, org_type, status)
VALUES (1, 'ORG001', '测试机构', '养老院', 1);

INSERT INTO department (id, org_id, parent_id, dept_name, dept_code, sort_no, status)
VALUES (10, 1, NULL, '护理部', 'D001', 1, 1);

INSERT INTO role (id, org_id, role_name, role_code, role_desc, status)
VALUES (100, 1, '管理员', 'ADMIN', '系统管理员', 1);
INSERT INTO role (id, org_id, role_name, role_code, role_desc, status)
VALUES (101, 1, '员工', 'STAFF', '普通员工', 1);

INSERT INTO staff (id, org_id, department_id, staff_no, username, password_hash, real_name, phone, email, gender, status)
VALUES (500, 1, 10, 'S0001', 'admin', '$2a$10$oHe4Uo7TeO5Ftwj0zK33auA8wXIMcbHScVrPTYbeTnrrrkbEJX.Qe', 'Admin', '13000000000', 'admin@test.com', 1, 1);
INSERT INTO staff (id, org_id, department_id, staff_no, username, password_hash, real_name, phone, email, gender, status)
VALUES (501, 1, 10, 'S0002', 'staff', '$2a$10$oHe4Uo7TeO5Ftwj0zK33auA8wXIMcbHScVrPTYbeTnrrrkbEJX.Qe', 'Staff', '13000000001', 'staff@test.com', 1, 1);

INSERT INTO staff_role (id, org_id, staff_id, role_id)
VALUES (1000, 1, 500, 100);
INSERT INTO staff_role (id, org_id, staff_id, role_id)
VALUES (1001, 1, 501, 101);

INSERT INTO care_task_template (id, org_id, task_name, frequency_per_day, care_level_required, enabled)
VALUES (300, 1, 'Temperature Check', 1, 'A', 1);

INSERT INTO care_task_daily (id, org_id, elder_id, bed_id, template_id, task_date, plan_time, status)
VALUES (400, 1, 200, 100, 300, CURRENT_DATE, CURRENT_TIMESTAMP, 'PENDING');

INSERT INTO care_task_daily (id, org_id, elder_id, bed_id, template_id, task_date, plan_time, status)
VALUES (401, 1, 200, 100, 300, CURRENT_DATE, CURRENT_TIMESTAMP, 'PENDING');

INSERT INTO care_task_execute_log (id, org_id, task_daily_id, elder_id, bed_id, staff_id, execute_time, bed_qr_code, result_status, suspicious_flag)
VALUES (600, 1, 401, 201, 101, 500, DATEADD('MINUTE', -2, CURRENT_TIMESTAMP), 'QR2', 1, 0);

-- 健康风控商城测试数据
INSERT INTO disease (id, org_id, disease_code, disease_name) VALUES (1001, 1, 'DIABETES', '糖尿病');
INSERT INTO disease (id, org_id, disease_code, disease_name) VALUES (1002, 1, 'GOUT', '痛风');

INSERT INTO product_tag (id, org_id, tag_code, tag_name) VALUES (2001, 1, 'SUGAR', '含糖');
INSERT INTO product_tag (id, org_id, tag_code, tag_name) VALUES (2002, 1, 'SEAFOOD', '海鲜');

INSERT INTO disease_forbidden_tag (id, org_id, disease_id, tag_id, forbidden_level)
VALUES (3001, 1, 1001, 2001, 1);
INSERT INTO disease_forbidden_tag (id, org_id, disease_id, tag_id, forbidden_level)
VALUES (3002, 1, 1002, 2002, 1);

INSERT INTO elder (id, org_id, elder_code, elder_qr_code, full_name, status) VALUES (4001, 1, 'E1001', 'EQR1001', '糖尿病老人', 1);
INSERT INTO elder (id, org_id, elder_code, elder_qr_code, full_name, status) VALUES (4002, 1, 'E1002', 'EQR1002', '痛风老人', 1);
INSERT INTO elder (id, org_id, elder_code, elder_qr_code, full_name, status) VALUES (4003, 1, 'E1003', 'EQR1003', '正常老人', 1);

INSERT INTO elder_disease (id, org_id, elder_id, disease_id) VALUES (5001, 1, 4001, 1001);
INSERT INTO elder_disease (id, org_id, elder_id, disease_id) VALUES (5002, 1, 4002, 1002);

INSERT INTO product (id, org_id, product_code, product_name, points_price, safety_stock, status, tag_ids)
VALUES (6001, 1, 'SUGAR01', '白砂糖', 10, 5, 1, '2001');
INSERT INTO product (id, org_id, product_code, product_name, points_price, safety_stock, status, tag_ids)
VALUES (6002, 1, 'SEA01', '海鲜拼盘', 30, 5, 1, '2002');
INSERT INTO product (id, org_id, product_code, product_name, points_price, safety_stock, status, tag_ids)
VALUES (6003, 1, 'MILK01', '纯牛奶', 5, 5, 1, NULL);

INSERT INTO elder_points_account (id, org_id, elder_id, points_balance, status) VALUES (7001, 1, 4001, 100, 1);
INSERT INTO elder_points_account (id, org_id, elder_id, points_balance, status) VALUES (7002, 1, 4002, 100, 1);
INSERT INTO elder_points_account (id, org_id, elder_id, points_balance, status) VALUES (7003, 1, 4003, 100, 1);

-- 库存批次（牛奶可售库存）
INSERT INTO inventory_batch (id, org_id, product_id, batch_no, quantity, cost_price, expire_date, warehouse_location)
VALUES (8001, 1, 6003, 'BATCH-MILK-01', 10, 2.50, DATEADD('DAY', 30, CURRENT_DATE), 'WH-A1');

-- 生命体征阈值配置
INSERT INTO vital_threshold_config (id, org_id, type, metric_code, min_value, max_value, status)
VALUES (9001, 1, 'TEMP', NULL, 36.0, 37.5, 1);
INSERT INTO vital_threshold_config (id, org_id, type, metric_code, min_value, max_value, status)
VALUES (9002, 1, 'HR', NULL, 60, 100, 1);
INSERT INTO vital_threshold_config (id, org_id, type, metric_code, min_value, max_value, status)
VALUES (9003, 1, 'BP', 'SBP', 90, 140, 1);
INSERT INTO vital_threshold_config (id, org_id, type, metric_code, min_value, max_value, status)
VALUES (9004, 1, 'BP', 'DBP', 60, 90, 1);
