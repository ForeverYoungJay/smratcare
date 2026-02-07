-- 月度账单统计示例（MySQL 8）

-- 1) 统计某老人某月商城消费
SELECT elder_id,
       DATE_FORMAT(create_time, '%Y-%m') AS bill_month,
       SUM(total_amount) AS shop_amount
FROM `order`
WHERE org_id = 1
  AND elder_id = 4003
  AND pay_status = 1
  AND create_time >= '2026-01-01'
  AND create_time < '2026-02-01'
GROUP BY elder_id, bill_month;

-- 2) 查询某月已生成账单（避免重复生成）
SELECT id, org_id, elder_id, bill_month
FROM bill_monthly
WHERE org_id = 1 AND bill_month = '2026-01';

-- 3) 查询某账单明细
SELECT item_type, item_name, amount, remark
FROM bill_item
WHERE bill_monthly_id = 123456;
