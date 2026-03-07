CREATE TABLE IF NOT EXISTS medical_alert_rule_config (
  id BIGINT PRIMARY KEY COMMENT '主键',
  tenant_id BIGINT DEFAULT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  medication_high_dosage_threshold INT NOT NULL DEFAULT 10 COMMENT '用药高剂量阈值',
  overdue_hours_threshold INT NOT NULL DEFAULT 12 COMMENT '超时阈值(小时)',
  abnormal_inspection_require_photo TINYINT NOT NULL DEFAULT 1 COMMENT '异常巡检是否必须上传照片',
  handover_auto_fill_confirm_time TINYINT NOT NULL DEFAULT 1 COMMENT '交接班是否自动补确认时间',
  auto_create_nursing_log_from_inspection TINYINT NOT NULL DEFAULT 1 COMMENT '巡检异常自动建护理日志',
  auto_raise_task_from_abnormal TINYINT NOT NULL DEFAULT 1 COMMENT '异常任务自动升级',
  auto_carry_resident_context TINYINT NOT NULL DEFAULT 1 COMMENT '跨页携带长者上下文',
  handover_risk_keywords VARCHAR(500) DEFAULT NULL COMMENT '交接风险关键词',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  updated_by BIGINT DEFAULT NULL COMMENT '更新人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
  UNIQUE KEY uk_medical_alert_rule_org (org_id)
) COMMENT='医护异常规则配置';
