CREATE TABLE IF NOT EXISTS crm_visit_appointment (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  org_id BIGINT NOT NULL,
  lead_id BIGINT NOT NULL,
  appointment_time TIMESTAMP NOT NULL,
  visitor_name VARCHAR(64) NOT NULL,
  visitor_phone VARCHAR(32),
  party_size INT NOT NULL DEFAULT 1,
  owner_staff_id BIGINT,
  owner_staff_name VARCHAR(64),
  status VARCHAR(32) NOT NULL DEFAULT 'SCHEDULED',
  arrived_at TIMESTAMP,
  completed_at TIMESTAMP,
  visit_result VARCHAR(255),
  cancel_reason VARCHAR(255),
  next_follow_date DATE,
  remark VARCHAR(255),
  created_by BIGINT,
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS crm_contract_reminder_config (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  org_id BIGINT NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  advance_days INT NOT NULL DEFAULT 30,
  send_time VARCHAR(8) NOT NULL DEFAULT '09:00',
  template_content VARCHAR(255) NOT NULL,
  created_by BIGINT,
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  CONSTRAINT uk_crm_contract_reminder_org UNIQUE (tenant_id, org_id, is_deleted)
);
