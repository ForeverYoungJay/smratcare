-- 巡诊记录（逐长者查体所见、处理意见，可关联生成病程记录/医嘱）
CREATE TABLE IF NOT EXISTS medical_rounds_record (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NULL,
  org_id BIGINT NOT NULL,
  plan_id BIGINT NULL,
  elder_id BIGINT NOT NULL,
  elder_name VARCHAR(64) NULL,
  round_time DATETIME NULL,
  findings VARCHAR(1000) NULL,
  handle_opinion VARCHAR(1000) NULL,
  result_level VARCHAR(16) NOT NULL DEFAULT 'NORMAL',
  emr_record_id BIGINT NULL,
  medical_order_id BIGINT NULL,
  doctor_id BIGINT NULL,
  doctor_name VARCHAR(60) NULL,
  remark VARCHAR(500) NULL,
  created_by BIGINT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_rounds_record_plan (org_id, plan_id),
  KEY idx_rounds_record_elder (org_id, elder_id, round_time)
);
