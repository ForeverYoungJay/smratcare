-- 经营驾驶舱 BI：指标定义登记表（指标口径治理，org_id 为空表示平台级统一口径）
CREATE TABLE IF NOT EXISTS stats_metric_definition (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NULL COMMENT '租户ID',
  org_id BIGINT NULL COMMENT '机构ID（NULL=平台级统一口径）',
  metric_code VARCHAR(64) NOT NULL COMMENT '指标编码',
  metric_name VARCHAR(128) NOT NULL COMMENT '指标名称',
  metric_category VARCHAR(32) NOT NULL DEFAULT 'OPERATION' COMMENT '指标分类：OPERATION运营/FINANCE财务/CARE护理安全',
  unit VARCHAR(16) NULL COMMENT '计量单位',
  caliber_desc VARCHAR(512) NULL COMMENT '口径说明',
  calc_sql_summary VARCHAR(512) NULL COMMENT '计算SQL摘要（口径追溯用，非可执行SQL）',
  source_table VARCHAR(128) NULL COMMENT '数据来源表',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_stats_metric_definition_code (metric_code, is_deleted),
  KEY idx_stats_metric_definition_category (metric_category)
) COMMENT='经营驾驶舱指标定义登记表';

-- 平台级指标口径登记（与预聚合任务口径一一对应）
INSERT INTO stats_metric_definition
  (id, org_id, metric_code, metric_name, metric_category, unit, caliber_desc, calc_sql_summary, source_table, status)
VALUES
  (2590001, NULL, 'OCCUPANCY_RATE', '入住率', 'OPERATION', '%', '占用床位数/总床位数*100，按日快照', 'SELECT COUNT(elder_id NOT NULL)/COUNT(*) FROM bed WHERE org_id=? AND is_deleted=0', 'bed', 1),
  (2590002, NULL, 'RESIDENT_COUNT', '在住人数', 'OPERATION', '人', '在住状态长者数（elder_profile.status=1）', 'SELECT COUNT(*) FROM elder_profile WHERE status=1 AND org_id=? AND is_deleted=0', 'elder_profile', 1),
  (2590003, NULL, 'ADMISSIONS', '当日入住数', 'OPERATION', '人', '入住日期等于统计日的长者数', 'SELECT COUNT(*) FROM elder_profile WHERE admission_date=?', 'elder_profile', 1),
  (2590004, NULL, 'DISCHARGES', '当日退住数', 'OPERATION', '人', '生命周期状态变更为退住/离世且变更时间落在统计日', 'SELECT COUNT(*) FROM elder_profile WHERE lifecycle_status IN (DISCHARGED,DECEASED) AND lifecycle_updated_at IN 统计日', 'elder_profile', 1),
  (2590005, NULL, 'BED_TURNOVER_RATE', '床位周转率', 'OPERATION', '%', '(当日入住数+当日退住数)/总床位数*100', '(admissions+discharges)/total_beds*100', 'bed,elder_profile', 1),
  (2590006, NULL, 'AVG_STAY_DAYS', '平均在住时长', 'OPERATION', '天', '在住长者自入住日至统计日天数的平均值', 'AVG(DATEDIFF(?, admission_date)) FROM elder_profile WHERE status=1', 'elder_profile', 1),
  (2590007, NULL, 'CARE_LEVEL_DIST', '护理等级分布', 'OPERATION', NULL, '在住长者按护理等级分组计数，JSON 存储', 'SELECT care_level, COUNT(*) FROM elder_profile WHERE status=1 GROUP BY care_level', 'elder_profile', 1),
  (2590011, NULL, 'REVENUE_AMOUNT', '当月营收', 'FINANCE', '元', '统计日所属账单月的月账单应收合计（含未回款）', 'SELECT SUM(total_amount) FROM bill_monthly WHERE bill_month=?', 'bill_monthly', 1),
  (2590012, NULL, 'PAID_AMOUNT', '当月回款', 'FINANCE', '元', '统计日所属账单月的月账单已回款合计', 'SELECT SUM(paid_amount) FROM bill_monthly WHERE bill_month=?', 'bill_monthly', 1),
  (2590013, NULL, 'PAID_DAILY_AMOUNT', '当日回款', 'FINANCE', '元', '支付时间落在统计日的收款流水合计', 'SELECT SUM(amount) FROM payment_record WHERE paid_at IN 统计日', 'payment_record', 1),
  (2590014, NULL, 'OUTSTANDING_AMOUNT', '当月欠费', 'FINANCE', '元', '统计日所属账单月的月账单欠费合计', 'SELECT SUM(outstanding_amount) FROM bill_monthly WHERE bill_month=?', 'bill_monthly', 1),
  (2590015, NULL, 'COLLECTION_RATE', '回款率', 'FINANCE', '%', '当月已回款/当月应收*100', 'SUM(paid_amount)/SUM(total_amount)*100', 'bill_monthly', 1),
  (2590016, NULL, 'COST_STRUCTURE', '费用科目结构', 'FINANCE', NULL, '当月账单明细按科目（item_type）汇总金额，JSON 存储', 'SELECT item_type, SUM(amount) FROM bill_item WHERE bill_monthly_id IN 当月账单 GROUP BY item_type', 'bill_item', 1),
  (2590021, NULL, 'CARE_TASK_COMPLETION_RATE', '护理工单完成率', 'CARE', '%', '当日已完成（DONE）护理工单/当日工单总数*100', 'SELECT COUNT(status=DONE)/COUNT(*) FROM care_task_daily WHERE task_date=?', 'care_task_daily', 1),
  (2590022, NULL, 'HOURS_PER_STAFF', '人效工时', 'CARE', '小时', '当日排班总工时/排班员工数（工时=排班结束-开始）', 'SUM(TIMESTAMPDIFF(HOUR,start_time,end_time))/COUNT(DISTINCT staff_id) FROM staff_schedule WHERE duty_date=?', 'staff_schedule', 1),
  (2590023, NULL, 'ALERT_ON_TIME_RATE', '告警响应达标率', 'CARE', '%', '当日触发告警中已响应且未超时升级（escalation_count=0）的占比', 'COUNT(status!=OPEN AND escalation_count=0)/COUNT(*) FROM smart_alert WHERE first_triggered_at IN 统计日', 'smart_alert', 1),
  (2590024, NULL, 'SATISFACTION_SCORE', '家属满意度', 'CARE', '分', '当日提交的满意度问卷有效得分平均值', 'AVG(score_effective) FROM survey_submission WHERE create_time IN 统计日', 'survey_submission', 1);
