-- 经营驾驶舱 BI：每日财务汇总（预聚合）
CREATE TABLE IF NOT EXISTS stats_daily_finance (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID（应用生成）',
  tenant_id BIGINT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  stat_date DATE NOT NULL COMMENT '统计日期',
  revenue_amount DECIMAL(14,2) NOT NULL DEFAULT 0 COMMENT '当月账单应收累计（bill_monthly.total_amount，统计日所属账单月）',
  paid_amount DECIMAL(14,2) NOT NULL DEFAULT 0 COMMENT '当月已回款累计（bill_monthly.paid_amount）',
  paid_daily_amount DECIMAL(14,2) NOT NULL DEFAULT 0 COMMENT '当日回款（payment_record.paid_at 落在统计日）',
  outstanding_amount DECIMAL(14,2) NOT NULL DEFAULT 0 COMMENT '当月欠费累计（bill_monthly.outstanding_amount）',
  collection_rate DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT '回款率% = 已回款/应收*100',
  cost_structure_json TEXT NULL COMMENT '当月费用科目结构 JSON（bill_item 按 item_type 汇总）：{"BED_FEE":12000.00,...}',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_stats_daily_finance_org_date (org_id, stat_date, is_deleted),
  KEY idx_stats_daily_finance_org_date (org_id, stat_date)
) COMMENT='经营驾驶舱每日财务汇总';
