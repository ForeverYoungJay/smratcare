-- M8 评估管理增强：量表模板、自动评分、复评提醒

SET @assessment_template_id_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'assessment_record'
    AND COLUMN_NAME = 'template_id'
);
SET @ddl = IF(
  @assessment_template_id_exists = 0,
  'ALTER TABLE assessment_record ADD COLUMN template_id BIGINT DEFAULT NULL COMMENT ''量表模板ID'' AFTER assessment_type',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @assessment_detail_json_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'assessment_record'
    AND COLUMN_NAME = 'detail_json'
);
SET @ddl = IF(
  @assessment_detail_json_exists = 0,
  'ALTER TABLE assessment_record ADD COLUMN detail_json TEXT DEFAULT NULL COMMENT ''评估明细JSON'' AFTER suggestion',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @assessment_score_auto_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'assessment_record'
    AND COLUMN_NAME = 'score_auto'
);
SET @ddl = IF(
  @assessment_score_auto_exists = 0,
  'ALTER TABLE assessment_record ADD COLUMN score_auto TINYINT NOT NULL DEFAULT 1 COMMENT ''是否自动评分 1是0否'' AFTER detail_json',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS assessment_scale_template (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  template_code VARCHAR(64) NOT NULL COMMENT '模板编码',
  template_name VARCHAR(128) NOT NULL COMMENT '模板名称',
  assessment_type VARCHAR(32) NOT NULL COMMENT '评估类型',
  description VARCHAR(255) DEFAULT NULL COMMENT '模板描述',
  score_rules_json TEXT DEFAULT NULL COMMENT '评分规则JSON',
  level_rules_json TEXT DEFAULT NULL COMMENT '等级规则JSON',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用0停用',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否1是',
  UNIQUE KEY uk_assessment_scale_tpl_code (org_id, template_code),
  KEY idx_assessment_scale_tpl_tenant_id (tenant_id),
  KEY idx_assessment_scale_tpl_org_id (org_id),
  KEY idx_assessment_scale_tpl_type (assessment_type),
  KEY idx_assessment_scale_tpl_status (status)
) COMMENT='评估量表模板';

CREATE TABLE IF NOT EXISTS assessment_reminder_log (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  record_id BIGINT NOT NULL COMMENT '评估记录ID',
  remind_date DATE NOT NULL COMMENT '提醒日期',
  todo_id BIGINT DEFAULT NULL COMMENT 'OA待办ID',
  status VARCHAR(16) NOT NULL DEFAULT 'CREATED' COMMENT '状态 CREATED/SKIPPED',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否1是',
  UNIQUE KEY uk_assessment_reminder_once (org_id, record_id, remind_date),
  KEY idx_assessment_reminder_tenant_id (tenant_id),
  KEY idx_assessment_reminder_org_id (org_id),
  KEY idx_assessment_reminder_date (remind_date)
) COMMENT='评估复评提醒日志';

UPDATE assessment_scale_template SET tenant_id = org_id WHERE tenant_id = 0;
UPDATE assessment_reminder_log SET tenant_id = org_id WHERE tenant_id = 0;

INSERT INTO assessment_scale_template (
  id, tenant_id, org_id, template_code, template_name, assessment_type,
  description, score_rules_json, level_rules_json, status, created_by, is_deleted
)
SELECT
  8000000000001, 1, 1, 'ADL_BARTHEL', 'Barthel自理能力量表', 'SELF_CARE',
  '日常生活活动能力评估',
  '[{"key":"feeding","weight":1},{"key":"bathing","weight":1},{"key":"grooming","weight":1},{"key":"dressing","weight":1},{"key":"toilet","weight":1},{"key":"transfer","weight":1},{"key":"mobility","weight":1},{"key":"stairs","weight":1}]',
  '[{"min":0,"max":20,"level":"重度依赖"},{"min":21,"max":60,"level":"中度依赖"},{"min":61,"max":99,"level":"轻度依赖"},{"min":100,"max":200,"level":"基本自理"}]',
  1, 1, 0
WHERE NOT EXISTS (
  SELECT 1 FROM assessment_scale_template WHERE org_id = 1 AND template_code = 'ADL_BARTHEL' AND is_deleted = 0
);

INSERT INTO assessment_scale_template (
  id, tenant_id, org_id, template_code, template_name, assessment_type,
  description, score_rules_json, level_rules_json, status, created_by, is_deleted
)
SELECT
  8000000000002, 1, 1, 'COG_MMSE', 'MMSE认知量表', 'COGNITIVE',
  '认知功能筛查评估',
  '[{"key":"orientation","weight":1},{"key":"registration","weight":1},{"key":"attention","weight":1},{"key":"recall","weight":1},{"key":"language","weight":1}]',
  '[{"min":0,"max":9,"level":"重度认知障碍"},{"min":10,"max":20,"level":"中度认知障碍"},{"min":21,"max":26,"level":"轻度认知障碍"},{"min":27,"max":40,"level":"认知基本正常"}]',
  1, 1, 0
WHERE NOT EXISTS (
  SELECT 1 FROM assessment_scale_template WHERE org_id = 1 AND template_code = 'COG_MMSE' AND is_deleted = 0
);

INSERT INTO assessment_record (
  id, tenant_id, org_id, elder_id, elder_name, assessment_type, template_id,
  level_code, score, assessment_date, next_assessment_date, assessor_id, assessor_name,
  status, result_summary, suggestion, detail_json, score_auto, archive_no, source, created_by, is_deleted
)
SELECT
  8100000000001, 1, 1, 1, '演示老人A', 'ADMISSION', NULL,
  'B', 78.00, CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY), 1, '系统管理员',
  'COMPLETED', '入住风险中等，建议重点关注夜间巡视。', '每周一次综合复评。',
  NULL, 0, 'ARC-ADM-0001', 'MANUAL', 1, 0
WHERE NOT EXISTS (
  SELECT 1 FROM assessment_record WHERE org_id = 1 AND archive_no = 'ARC-ADM-0001' AND is_deleted = 0
);

INSERT INTO assessment_record (
  id, tenant_id, org_id, elder_id, elder_name, assessment_type, template_id,
  level_code, score, assessment_date, next_assessment_date, assessor_id, assessor_name,
  status, result_summary, suggestion, detail_json, score_auto, archive_no, source, created_by, is_deleted
)
SELECT
  8100000000002, 1, 1, 1, '演示老人A', 'SELF_CARE', 8000000000001,
  '轻度依赖', 68.00, CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 14 DAY), 1, '系统管理员',
  'COMPLETED', '具备基础活动能力，部分动作需辅助。', '维持步行训练与上下楼练习。',
  '{"feeding":10,"bathing":5,"grooming":5,"dressing":10,"toilet":10,"transfer":10,"mobility":10,"stairs":8}',
  1, 'ARC-SEL-0001', 'MANUAL', 1, 0
WHERE NOT EXISTS (
  SELECT 1 FROM assessment_record WHERE org_id = 1 AND archive_no = 'ARC-SEL-0001' AND is_deleted = 0
);
