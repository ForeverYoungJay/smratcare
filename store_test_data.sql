-- 健康风控商城测试数据

-- 疾病
INSERT INTO disease (id, org_id, disease_code, disease_name) VALUES (1001, 1, 'DIABETES', '糖尿病');
INSERT INTO disease (id, org_id, disease_code, disease_name) VALUES (1002, 1, 'GOUT', '痛风');

-- 商品标签
INSERT INTO product_tag (id, org_id, tag_code, tag_name) VALUES (2001, 1, 'SUGAR', '含糖');
INSERT INTO product_tag (id, org_id, tag_code, tag_name) VALUES (2002, 1, 'SEAFOOD', '海鲜');

-- 禁忌规则：糖尿病禁糖，痛风禁海鲜
INSERT INTO disease_forbidden_tag (id, org_id, disease_id, tag_id, forbidden_level)
VALUES (3001, 1, 1001, 2001, 1);
INSERT INTO disease_forbidden_tag (id, org_id, disease_id, tag_id, forbidden_level)
VALUES (3002, 1, 1002, 2002, 1);

-- 老人
INSERT INTO elder (id, org_id, full_name, status) VALUES (4001, 1, '糖尿病老人', 1);
INSERT INTO elder (id, org_id, full_name, status) VALUES (4002, 1, '痛风老人', 1);
INSERT INTO elder (id, org_id, full_name, status) VALUES (4003, 1, '正常老人', 1);

-- 老人疾病关联
INSERT INTO elder_disease (id, org_id, elder_id, disease_id) VALUES (5001, 1, 4001, 1001);
INSERT INTO elder_disease (id, org_id, elder_id, disease_id) VALUES (5002, 1, 4002, 1002);

-- 商品
INSERT INTO product (id, org_id, product_code, product_name, points_price, status, tag_ids)
VALUES (6001, 1, 'SUGAR01', '白砂糖', 10, 1, '2001');
INSERT INTO product (id, org_id, product_code, product_name, points_price, status, tag_ids)
VALUES (6002, 1, 'SEA01', '海鲜拼盘', 30, 1, '2002');
INSERT INTO product (id, org_id, product_code, product_name, points_price, status, tag_ids)
VALUES (6003, 1, 'MILK01', '纯牛奶', 5, 1, NULL);

-- 积分账户
INSERT INTO elder_points_account (id, org_id, elder_id, points_balance, status) VALUES (7001, 1, 4001, 100, 1);
INSERT INTO elder_points_account (id, org_id, elder_id, points_balance, status) VALUES (7002, 1, 4002, 100, 1);
INSERT INTO elder_points_account (id, org_id, elder_id, points_balance, status) VALUES (7003, 1, 4003, 100, 1);
