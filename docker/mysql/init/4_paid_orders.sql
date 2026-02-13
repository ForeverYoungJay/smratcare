-- Paid orders sample data (status: PAID)
INSERT INTO `order` (
  id, org_id, order_no, elder_id, total_amount, points_used,
  payable_amount, pay_status, pay_time, order_status, create_time, update_time, is_deleted
) VALUES
  (91011, 1, 'ORD20260208-PAID-011', 4001, 10.00, 10, 10.00, 1, NOW(), 2, NOW(), NOW(), 0),
  (91012, 1, 'ORD20260208-PAID-012', 4002, 30.00, 30, 30.00, 1, NOW(), 2, NOW(), NOW(), 0),
  (91013, 1, 'ORD20260208-PAID-013', 4003, 5.00, 5, 5.00, 1, NOW(), 2, NOW(), NOW(), 0);

INSERT INTO order_item (
  id, org_id, order_id, product_id, product_name, unit_price, quantity, total_amount, create_time, update_time, is_deleted
) VALUES
  (92011, 1, 91011, 6001, '白砂糖', 10.00, 1, 10.00, NOW(), NOW(), 0),
  (92012, 1, 91012, 6002, '海鲜拼盘', 30.00, 1, 30.00, NOW(), NOW(), 0),
  (92013, 1, 91013, 6003, '纯牛奶', 5.00, 1, 5.00, NOW(), NOW(), 0);
