-- H2 测试库补建 IoT 安全预警闭环相关表（主库定义见 V205/V230/V231/V232-V234）
-- 以及员工排班表（主库定义见 V7），供告警自动派单当班查询使用。

CREATE TABLE IF NOT EXISTS smart_device (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT DEFAULT NULL,
  org_id BIGINT NOT NULL,
  elder_id BIGINT DEFAULT NULL,
  device_code VARCHAR(64) NOT NULL,
  device_name VARCHAR(120) NOT NULL,
  device_type VARCHAR(64) NOT NULL,
  vendor VARCHAR(120) DEFAULT NULL,
  model VARCHAR(120) DEFAULT NULL,
  location VARCHAR(180) DEFAULT NULL,
  protocol VARCHAR(32) DEFAULT NULL,
  bind_status VARCHAR(32) NOT NULL DEFAULT 'BOUND',
  online_status VARCHAR(32) NOT NULL DEFAULT 'OFFLINE',
  last_heartbeat_at DATETIME DEFAULT NULL,
  last_event_at DATETIME DEFAULT NULL,
  battery_level INT DEFAULT NULL,
  signal_strength INT DEFAULT NULL,
  firmware_version VARCHAR(64) DEFAULT NULL,
  enabled TINYINT NOT NULL DEFAULT 1,
  remark VARCHAR(500) DEFAULT NULL,
  created_by BIGINT DEFAULT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS smart_device_event (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT DEFAULT NULL,
  org_id BIGINT NOT NULL,
  device_id BIGINT DEFAULT NULL,
  elder_id BIGINT DEFAULT NULL,
  device_code VARCHAR(64) NOT NULL,
  event_type VARCHAR(64) NOT NULL,
  event_level VARCHAR(32) NOT NULL DEFAULT 'INFO',
  event_time DATETIME NOT NULL,
  payload_json TEXT DEFAULT NULL,
  location VARCHAR(180) DEFAULT NULL,
  handled TINYINT NOT NULL DEFAULT 0,
  generated_alert_id BIGINT DEFAULT NULL,
  created_by BIGINT DEFAULT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS smart_alert (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT DEFAULT NULL,
  org_id BIGINT NOT NULL,
  elder_id BIGINT DEFAULT NULL,
  device_id BIGINT DEFAULT NULL,
  source_event_id BIGINT DEFAULT NULL,
  alert_no VARCHAR(64) NOT NULL,
  alert_type VARCHAR(64) NOT NULL,
  level VARCHAR(32) NOT NULL,
  title VARCHAR(160) NOT NULL,
  content VARCHAR(1000) DEFAULT NULL,
  location VARCHAR(180) DEFAULT NULL,
  media_ref VARCHAR(500) DEFAULT NULL,
  location_ref VARCHAR(500) DEFAULT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
  first_triggered_at DATETIME NOT NULL,
  latest_triggered_at DATETIME NOT NULL,
  acknowledged_by BIGINT DEFAULT NULL,
  acknowledged_at DATETIME DEFAULT NULL,
  resolved_by BIGINT DEFAULT NULL,
  resolved_at DATETIME DEFAULT NULL,
  resolution_note VARCHAR(1000) DEFAULT NULL,
  escalation_count INT NOT NULL DEFAULT 0,
  notify_family TINYINT NOT NULL DEFAULT 0,
  created_by BIGINT DEFAULT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS smart_alert_rule (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT DEFAULT NULL,
  org_id BIGINT DEFAULT NULL,
  rule_code VARCHAR(64) NOT NULL,
  rule_name VARCHAR(150) NOT NULL,
  event_type VARCHAR(64) NOT NULL,
  device_type VARCHAR(64) DEFAULT NULL,
  metric_key VARCHAR(64) DEFAULT NULL,
  operator VARCHAR(16) NOT NULL DEFAULT 'PRESENT',
  threshold DECIMAL(12,2) DEFAULT NULL,
  threshold2 DECIMAL(12,2) DEFAULT NULL,
  duration_sec INT DEFAULT NULL,
  level VARCHAR(16) NOT NULL DEFAULT 'HIGH',
  disability_level_scope VARCHAR(64) DEFAULT NULL,
  care_level_scope VARCHAR(64) DEFAULT NULL,
  auto_dispatch TINYINT NOT NULL DEFAULT 1,
  notify_family TINYINT NOT NULL DEFAULT 0,
  priority INT NOT NULL DEFAULT 100,
  enabled TINYINT NOT NULL DEFAULT 1,
  remark VARCHAR(500) DEFAULT NULL,
  created_by BIGINT DEFAULT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS smart_alert_dispatch (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT DEFAULT NULL,
  org_id BIGINT NOT NULL,
  alert_id BIGINT NOT NULL,
  elder_id BIGINT DEFAULT NULL,
  device_id BIGINT DEFAULT NULL,
  rule_id BIGINT DEFAULT NULL,
  level VARCHAR(16) NOT NULL DEFAULT 'HIGH',
  dispatch_status VARCHAR(16) NOT NULL DEFAULT 'TRIGGERED',
  assignee_id BIGINT DEFAULT NULL,
  assignee_name VARCHAR(60) DEFAULT NULL,
  triggered_at DATETIME DEFAULT NULL,
  assigned_at DATETIME DEFAULT NULL,
  responded_at DATETIME DEFAULT NULL,
  onsite_at DATETIME DEFAULT NULL,
  handled_at DATETIME DEFAULT NULL,
  reviewed_at DATETIME DEFAULT NULL,
  response_deadline DATETIME DEFAULT NULL,
  escalation_count INT NOT NULL DEFAULT 0,
  escalated_to_id BIGINT DEFAULT NULL,
  escalated_to_name VARCHAR(60) DEFAULT NULL,
  escalated_at DATETIME DEFAULT NULL,
  handle_note VARCHAR(1000) DEFAULT NULL,
  review_note VARCHAR(1000) DEFAULT NULL,
  incident_id BIGINT DEFAULT NULL,
  created_by BIGINT DEFAULT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS staff_schedule (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  org_id BIGINT NOT NULL,
  staff_id BIGINT NOT NULL,
  source_template_id BIGINT DEFAULT NULL,
  source_template_name VARCHAR(120) DEFAULT NULL,
  duty_date DATE NOT NULL,
  shift_code VARCHAR(32) DEFAULT NULL,
  start_time DATETIME DEFAULT NULL,
  end_time DATETIME DEFAULT NULL,
  calendar_task_id BIGINT DEFAULT NULL,
  duty_todo_id BIGINT DEFAULT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  created_by BIGINT DEFAULT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

-- 闭环测试基础数据：护理员/护理部长角色与账号（独立新增，不影响既有种子）
INSERT INTO role (id, org_id, role_name, role_code, role_desc, status)
SELECT 102, 1, '护理员', 'NURSING_EMPLOYEE', '值班护理员', 1
WHERE NOT EXISTS (SELECT 1 FROM role WHERE id = 102);

INSERT INTO role (id, org_id, role_name, role_code, role_desc, status)
SELECT 103, 1, '护理部长', 'NURSING_MINISTER', '护理部长', 1
WHERE NOT EXISTS (SELECT 1 FROM role WHERE id = 103);

INSERT INTO staff (id, org_id, department_id, staff_no, username, password_hash, real_name, phone, email, gender, status)
SELECT 502, 1, 10, 'S0003', 'nurse1', '$2a$10$oHe4Uo7TeO5Ftwj0zK33auA8wXIMcbHScVrPTYbeTnrrrkbEJX.Qe', '值班护理员甲', '13000000002', 'nurse1@test.com', 2, 1
WHERE NOT EXISTS (SELECT 1 FROM staff WHERE id = 502);

INSERT INTO staff (id, org_id, department_id, staff_no, username, password_hash, real_name, phone, email, gender, status)
SELECT 503, 1, 10, 'S0004', 'minister1', '$2a$10$oHe4Uo7TeO5Ftwj0zK33auA8wXIMcbHScVrPTYbeTnrrrkbEJX.Qe', '护理部长乙', '13000000003', 'minister1@test.com', 2, 1
WHERE NOT EXISTS (SELECT 1 FROM staff WHERE id = 503);

INSERT INTO staff_role (id, org_id, staff_id, role_id)
SELECT 1002, 1, 502, 102
WHERE NOT EXISTS (SELECT 1 FROM staff_role WHERE id = 1002);

INSERT INTO staff_role (id, org_id, staff_id, role_id)
SELECT 1003, 1, 503, 103
WHERE NOT EXISTS (SELECT 1 FROM staff_role WHERE id = 1003);

-- 今日全天排班：护理员甲当班
INSERT INTO staff_schedule (id, tenant_id, org_id, staff_id, duty_date, shift_code, start_time, end_time, status)
SELECT 9001, 1, 1, 502, CURRENT_DATE, 'FULL', NULL, NULL, 1
WHERE NOT EXISTS (SELECT 1 FROM staff_schedule WHERE id = 9001);

-- 内置全局场景规则（对齐 V230/V233 标准枚举）
INSERT INTO smart_alert_rule (id, org_id, rule_code, rule_name, event_type, operator, level, auto_dispatch, notify_family, priority, enabled, remark)
SELECT 1, NULL, 'FALL', '跌倒告警', 'FALL', 'PRESENT', 'CRITICAL', 1, 1, 10, 1, '跌倒检测事件立即告警并派单'
WHERE NOT EXISTS (SELECT 1 FROM smart_alert_rule WHERE id = 1);

INSERT INTO smart_alert_rule (id, org_id, rule_code, rule_name, event_type, operator, level, auto_dispatch, notify_family, priority, enabled, remark)
SELECT 2, NULL, 'SOS', 'SOS一键呼叫', 'SOS', 'PRESENT', 'CRITICAL', 1, 1, 10, 1, '一键呼叫立即告警'
WHERE NOT EXISTS (SELECT 1 FROM smart_alert_rule WHERE id = 2);

INSERT INTO smart_alert_rule (id, org_id, rule_code, rule_name, event_type, metric_key, operator, threshold, level, auto_dispatch, notify_family, priority, enabled, remark)
SELECT 8, NULL, 'SPO2_LOW', '血氧过低', 'VITAL', 'spo2', 'LT', 90, 'CRITICAL', 1, 1, 20, 1, '血氧饱和度低于阈值'
WHERE NOT EXISTS (SELECT 1 FROM smart_alert_rule WHERE id = 8);
