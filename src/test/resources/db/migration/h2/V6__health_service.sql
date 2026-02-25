CREATE TABLE health_drug_dictionary (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  org_id BIGINT NOT NULL,
  drug_code VARCHAR(64),
  drug_name VARCHAR(128) NOT NULL,
  specification VARCHAR(128),
  unit VARCHAR(32),
  manufacturer VARCHAR(128),
  category VARCHAR(64),
  remark VARCHAR(500),
  created_by BIGINT,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE health_medication_deposit (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  org_id BIGINT NOT NULL,
  elder_id BIGINT NOT NULL,
  elder_name VARCHAR(64) NOT NULL,
  drug_id BIGINT,
  drug_name VARCHAR(128) NOT NULL,
  deposit_date DATE NOT NULL,
  quantity DECIMAL(12,2) NOT NULL,
  unit VARCHAR(32),
  expire_date DATE,
  depositor_name VARCHAR(64),
  remark VARCHAR(500),
  created_by BIGINT,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE health_medication_setting (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  org_id BIGINT NOT NULL,
  elder_id BIGINT NOT NULL,
  elder_name VARCHAR(64) NOT NULL,
  drug_id BIGINT,
  drug_name VARCHAR(128) NOT NULL,
  dosage VARCHAR(64),
  frequency VARCHAR(64),
  medication_time VARCHAR(128),
  start_date DATE,
  end_date DATE,
  min_remain_qty DECIMAL(12,2),
  remark VARCHAR(500),
  created_by BIGINT,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE health_medication_registration (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  org_id BIGINT NOT NULL,
  elder_id BIGINT NOT NULL,
  elder_name VARCHAR(64) NOT NULL,
  drug_id BIGINT,
  drug_name VARCHAR(128) NOT NULL,
  register_time DATETIME NOT NULL,
  dosage_taken DECIMAL(12,2) NOT NULL,
  unit VARCHAR(32),
  nurse_name VARCHAR(64),
  remark VARCHAR(500),
  created_by BIGINT,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE health_archive (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  org_id BIGINT NOT NULL,
  elder_id BIGINT NOT NULL,
  elder_name VARCHAR(64) NOT NULL,
  blood_type VARCHAR(16),
  allergy_history VARCHAR(500),
  chronic_disease VARCHAR(500),
  medical_history VARCHAR(500),
  emergency_contact VARCHAR(64),
  emergency_phone VARCHAR(32),
  remark VARCHAR(500),
  created_by BIGINT,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE health_data_record (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  org_id BIGINT NOT NULL,
  elder_id BIGINT NOT NULL,
  elder_name VARCHAR(64) NOT NULL,
  data_type VARCHAR(64) NOT NULL,
  data_value VARCHAR(256) NOT NULL,
  measured_at DATETIME NOT NULL,
  source VARCHAR(64),
  abnormal_flag TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500),
  created_by BIGINT,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE health_inspection (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  org_id BIGINT NOT NULL,
  elder_id BIGINT NOT NULL,
  elder_name VARCHAR(64) NOT NULL,
  inspection_date DATE NOT NULL,
  inspection_item VARCHAR(128) NOT NULL,
  result VARCHAR(500),
  status VARCHAR(32),
  inspector_name VARCHAR(64),
  follow_up_action VARCHAR(500),
  remark VARCHAR(500),
  created_by BIGINT,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE health_nursing_log (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  org_id BIGINT NOT NULL,
  elder_id BIGINT NOT NULL,
  elder_name VARCHAR(64) NOT NULL,
  source_inspection_id BIGINT,
  log_time DATETIME NOT NULL,
  log_type VARCHAR(64) NOT NULL,
  content VARCHAR(1000) NOT NULL,
  staff_name VARCHAR(64),
  status VARCHAR(32),
  remark VARCHAR(500),
  created_by BIGINT,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE health_medication_task (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  org_id BIGINT NOT NULL,
  setting_id BIGINT NOT NULL,
  elder_id BIGINT NOT NULL,
  elder_name VARCHAR(64) NOT NULL,
  drug_id BIGINT,
  drug_name VARCHAR(128) NOT NULL,
  planned_time DATETIME NOT NULL,
  task_date DATE NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  registration_id BIGINT,
  done_time DATETIME,
  remark VARCHAR(500),
  created_by BIGINT,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);
