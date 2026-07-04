-- 智慧安全场景规则引擎：可按事件类型/设备类型/失能等级配置阈值与告警等级
CREATE TABLE IF NOT EXISTS smart_alert_rule (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NULL,
  org_id BIGINT NULL,
  rule_code VARCHAR(64) NOT NULL,
  rule_name VARCHAR(150) NOT NULL,
  event_type VARCHAR(64) NOT NULL,
  device_type VARCHAR(64) NULL,
  metric_key VARCHAR(64) NULL,
  operator VARCHAR(16) NOT NULL DEFAULT 'PRESENT',
  threshold DECIMAL(12,2) NULL,
  threshold2 DECIMAL(12,2) NULL,
  duration_sec INT NULL,
  level VARCHAR(16) NOT NULL DEFAULT 'HIGH',
  disability_level_scope VARCHAR(64) NULL,
  auto_dispatch TINYINT NOT NULL DEFAULT 1,
  notify_family TINYINT NOT NULL DEFAULT 0,
  priority INT NOT NULL DEFAULT 100,
  enabled TINYINT NOT NULL DEFAULT 1,
  remark VARCHAR(500) NULL,
  created_by BIGINT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_smart_rule_code (org_id, rule_code, is_deleted),
  KEY idx_smart_rule_event (event_type, enabled),
  KEY idx_smart_rule_org (org_id, enabled)
);

-- 内置默认场景规则（org_id 为 NULL 表示全局默认；operator=PRESENT 表示事件出现即触发）
INSERT INTO smart_alert_rule (id, org_id, rule_code, rule_name, event_type, operator, level, duration_sec, auto_dispatch, notify_family, priority, enabled, remark)
SELECT 1, NULL, 'FALL', '跌倒告警', 'FALL', 'PRESENT', 'CRITICAL', NULL, 1, 1, 10, 1, '跌倒检测事件立即告警并派单'
WHERE NOT EXISTS (SELECT 1 FROM smart_alert_rule WHERE id = 1);

INSERT INTO smart_alert_rule (id, org_id, rule_code, rule_name, event_type, operator, level, duration_sec, auto_dispatch, notify_family, priority, enabled, remark)
SELECT 2, NULL, 'SOS', 'SOS一键呼叫', 'SOS', 'PRESENT', 'CRITICAL', NULL, 1, 1, 10, 1, '一键呼叫立即告警'
WHERE NOT EXISTS (SELECT 1 FROM smart_alert_rule WHERE id = 2);

INSERT INTO smart_alert_rule (id, org_id, rule_code, rule_name, event_type, operator, level, duration_sec, auto_dispatch, notify_family, priority, enabled, remark)
SELECT 3, NULL, 'LEAVE_BED', '离床超时', 'BED_EXIT', 'PRESENT', 'HIGH', 600, 1, 0, 30, 1, '离床超过阈值时长未返回'
WHERE NOT EXISTS (SELECT 1 FROM smart_alert_rule WHERE id = 3);

INSERT INTO smart_alert_rule (id, org_id, rule_code, rule_name, event_type, operator, level, duration_sec, auto_dispatch, notify_family, priority, enabled, remark)
SELECT 4, NULL, 'WANDER', '离院防走失', 'LEAVE', 'PRESENT', 'CRITICAL', NULL, 1, 1, 10, 1, '越界/离院告警'
WHERE NOT EXISTS (SELECT 1 FROM smart_alert_rule WHERE id = 4);

INSERT INTO smart_alert_rule (id, org_id, rule_code, rule_name, event_type, operator, level, duration_sec, auto_dispatch, notify_family, priority, enabled, remark)
SELECT 5, NULL, 'STAY_TOO_LONG', '卫生间久滞', 'STAY', 'PRESENT', 'HIGH', 1200, 1, 0, 40, 1, '卫生间/房间滞留超时'
WHERE NOT EXISTS (SELECT 1 FROM smart_alert_rule WHERE id = 5);

INSERT INTO smart_alert_rule (id, org_id, rule_code, rule_name, event_type, metric_key, operator, threshold, level, auto_dispatch, notify_family, priority, enabled, remark)
SELECT 6, NULL, 'HR_HIGH', '心率过高', 'VITAL', 'heartRate', 'GT', 120, 'HIGH', 1, 1, 50, 1, '心率高于阈值'
WHERE NOT EXISTS (SELECT 1 FROM smart_alert_rule WHERE id = 6);

INSERT INTO smart_alert_rule (id, org_id, rule_code, rule_name, event_type, metric_key, operator, threshold, level, auto_dispatch, notify_family, priority, enabled, remark)
SELECT 7, NULL, 'HR_LOW', '心率过低', 'VITAL', 'heartRate', 'LT', 45, 'HIGH', 1, 1, 50, 1, '心率低于阈值'
WHERE NOT EXISTS (SELECT 1 FROM smart_alert_rule WHERE id = 7);

INSERT INTO smart_alert_rule (id, org_id, rule_code, rule_name, event_type, metric_key, operator, threshold, level, auto_dispatch, notify_family, priority, enabled, remark)
SELECT 8, NULL, 'SPO2_LOW', '血氧过低', 'VITAL', 'spo2', 'LT', 90, 'CRITICAL', 1, 1, 20, 1, '血氧饱和度低于阈值'
WHERE NOT EXISTS (SELECT 1 FROM smart_alert_rule WHERE id = 8);
