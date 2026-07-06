-- 经营驾驶舱 BI 汇总表（对应主库 V256~V259，H2 兼容版）
CREATE TABLE IF NOT EXISTS stats_daily_operation (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT,
  org_id BIGINT NOT NULL,
  stat_date DATE NOT NULL,
  resident_count INT DEFAULT 0,
  total_beds INT DEFAULT 0,
  occupied_beds INT DEFAULT 0,
  occupancy_rate DECIMAL(5,2) DEFAULT 0,
  bed_turnover_rate DECIMAL(5,2) DEFAULT 0,
  admissions INT DEFAULT 0,
  discharges INT DEFAULT 0,
  avg_stay_days DECIMAL(8,1) DEFAULT 0,
  care_level_json CLOB,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_stats_daily_operation_org_date ON stats_daily_operation (org_id, stat_date);

CREATE TABLE IF NOT EXISTS stats_daily_finance (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT,
  org_id BIGINT NOT NULL,
  stat_date DATE NOT NULL,
  revenue_amount DECIMAL(14,2) DEFAULT 0,
  paid_amount DECIMAL(14,2) DEFAULT 0,
  paid_daily_amount DECIMAL(14,2) DEFAULT 0,
  outstanding_amount DECIMAL(14,2) DEFAULT 0,
  collection_rate DECIMAL(5,2) DEFAULT 0,
  cost_structure_json CLOB,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_stats_daily_finance_org_date ON stats_daily_finance (org_id, stat_date);

CREATE TABLE IF NOT EXISTS stats_daily_care (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT,
  org_id BIGINT NOT NULL,
  stat_date DATE NOT NULL,
  care_task_total INT DEFAULT 0,
  care_task_done INT DEFAULT 0,
  care_task_completion_rate DECIMAL(5,2) DEFAULT 0,
  scheduled_staff_count INT DEFAULT 0,
  scheduled_hours DECIMAL(10,1) DEFAULT 0,
  hours_per_staff DECIMAL(8,1) DEFAULT 0,
  alert_total INT DEFAULT 0,
  alert_on_time INT DEFAULT 0,
  alert_on_time_rate DECIMAL(5,2) DEFAULT 0,
  satisfaction_score DECIMAL(5,1) DEFAULT 0,
  satisfaction_count INT DEFAULT 0,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_stats_daily_care_org_date ON stats_daily_care (org_id, stat_date);

CREATE TABLE IF NOT EXISTS stats_metric_definition (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT,
  org_id BIGINT,
  metric_code VARCHAR(64) NOT NULL,
  metric_name VARCHAR(128) NOT NULL,
  metric_category VARCHAR(32) DEFAULT 'OPERATION',
  unit VARCHAR(16),
  caliber_desc VARCHAR(512),
  calc_sql_summary VARCHAR(512),
  source_table VARCHAR(128),
  status TINYINT DEFAULT 1,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_stats_metric_definition_category ON stats_metric_definition (metric_category);
