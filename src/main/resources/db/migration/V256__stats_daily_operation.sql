-- 经营驾驶舱 BI：每日运营汇总（预聚合，大屏只查汇总表不直查业务库）
CREATE TABLE IF NOT EXISTS stats_daily_operation (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID（应用生成）',
  tenant_id BIGINT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  stat_date DATE NOT NULL COMMENT '统计日期',
  resident_count INT NOT NULL DEFAULT 0 COMMENT '在住人数（elder_profile.status=1）',
  total_beds INT NOT NULL DEFAULT 0 COMMENT '总床位数',
  occupied_beds INT NOT NULL DEFAULT 0 COMMENT '占用床位数',
  occupancy_rate DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT '入住率% = 占用床位/总床位*100',
  bed_turnover_rate DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT '床位周转率% = (当日入住+当日退住)/总床位*100',
  admissions INT NOT NULL DEFAULT 0 COMMENT '当日入住数（admission_date=统计日）',
  discharges INT NOT NULL DEFAULT 0 COMMENT '当日退住数（含离世，lifecycle_updated_at 落在统计日）',
  avg_stay_days DECIMAL(8,1) NOT NULL DEFAULT 0 COMMENT '在住长者平均在住时长（天）',
  care_level_json TEXT NULL COMMENT '在住长者护理等级分布 JSON：{"一级":10,...}',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_stats_daily_operation_org_date (org_id, stat_date, is_deleted),
  KEY idx_stats_daily_operation_org_date (org_id, stat_date)
) COMMENT='经营驾驶舱每日运营汇总';
