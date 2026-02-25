-- M8 评估管理

CREATE TABLE IF NOT EXISTS assessment_record (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  elder_id BIGINT DEFAULT NULL COMMENT '老人ID',
  elder_name VARCHAR(64) NOT NULL COMMENT '老人姓名',
  assessment_type VARCHAR(32) NOT NULL COMMENT '评估类型 ADMISSION/REGISTRATION/CONTINUOUS/ARCHIVE/OTHER_SCALE/COGNITIVE/SELF_CARE',
  level_code VARCHAR(32) DEFAULT NULL COMMENT '评估等级',
  score DECIMAL(8,2) DEFAULT NULL COMMENT '评估分值',
  assessment_date DATE NOT NULL COMMENT '评估日期',
  next_assessment_date DATE DEFAULT NULL COMMENT '下次评估日期',
  assessor_id BIGINT DEFAULT NULL COMMENT '评估人ID',
  assessor_name VARCHAR(64) DEFAULT NULL COMMENT '评估人姓名',
  status VARCHAR(16) NOT NULL DEFAULT 'COMPLETED' COMMENT '状态 DRAFT/COMPLETED/ARCHIVED',
  result_summary VARCHAR(500) DEFAULT NULL COMMENT '评估结论',
  suggestion VARCHAR(500) DEFAULT NULL COMMENT '护理建议',
  archive_no VARCHAR(64) DEFAULT NULL COMMENT '档案编号',
  source VARCHAR(32) DEFAULT NULL COMMENT '来源 MANUAL/SYSTEM',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_assessment_tenant_id (tenant_id),
  KEY idx_assessment_org_id (org_id),
  KEY idx_assessment_elder_id (elder_id),
  KEY idx_assessment_type (assessment_type),
  KEY idx_assessment_date (assessment_date),
  KEY idx_assessment_status (status)
) COMMENT='评估记录';

UPDATE assessment_record SET tenant_id = org_id WHERE tenant_id = 0;
