-- 医生巡诊排班计划（医生、日期、楼层/区域、长者范围）
CREATE TABLE IF NOT EXISTS medical_rounds_plan (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NULL,
  org_id BIGINT NOT NULL,
  plan_date DATE NOT NULL,
  time_slot VARCHAR(32) NULL,
  doctor_id BIGINT NULL,
  doctor_name VARCHAR(60) NULL,
  area VARCHAR(120) NULL,
  elder_scope VARCHAR(16) NOT NULL DEFAULT 'AREA',
  elder_ids_json VARCHAR(2000) NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'PLANNED',
  remark VARCHAR(500) NULL,
  created_by BIGINT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_rounds_plan_date (org_id, plan_date, status),
  KEY idx_rounds_plan_doctor (org_id, doctor_id, plan_date)
);
