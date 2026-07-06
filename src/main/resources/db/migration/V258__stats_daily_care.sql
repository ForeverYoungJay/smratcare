-- 经营驾驶舱 BI：每日护理与安全汇总（预聚合）
CREATE TABLE IF NOT EXISTS stats_daily_care (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID（应用生成）',
  tenant_id BIGINT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  stat_date DATE NOT NULL COMMENT '统计日期',
  care_task_total INT NOT NULL DEFAULT 0 COMMENT '当日护理工单总数（care_task_daily.task_date=统计日）',
  care_task_done INT NOT NULL DEFAULT 0 COMMENT '当日已完成护理工单数（status=DONE）',
  care_task_completion_rate DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT '护理工单完成率% = 已完成/总数*100',
  scheduled_staff_count INT NOT NULL DEFAULT 0 COMMENT '当日排班员工数（staff_schedule 去重 staff_id）',
  scheduled_hours DECIMAL(10,1) NOT NULL DEFAULT 0 COMMENT '当日排班总工时（小时）',
  hours_per_staff DECIMAL(8,1) NOT NULL DEFAULT 0 COMMENT '人效工时 = 排班总工时/排班员工数',
  alert_total INT NOT NULL DEFAULT 0 COMMENT '当日告警总数（smart_alert.first_triggered_at 落在统计日）',
  alert_on_time INT NOT NULL DEFAULT 0 COMMENT '当日响应达标告警数（已响应且未升级）',
  alert_on_time_rate DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT '告警响应达标率% = 达标/总数*100',
  satisfaction_score DECIMAL(5,1) NOT NULL DEFAULT 0 COMMENT '当日满意度问卷平均分（survey_submission.score_effective）',
  satisfaction_count INT NOT NULL DEFAULT 0 COMMENT '当日满意度问卷提交数',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_stats_daily_care_org_date (org_id, stat_date, is_deleted),
  KEY idx_stats_daily_care_org_date (org_id, stat_date)
) COMMENT='经营驾驶舱每日护理与安全汇总';
