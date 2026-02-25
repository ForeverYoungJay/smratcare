-- M7 问卷与持续改进

CREATE TABLE IF NOT EXISTS survey_question_bank (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  question_code VARCHAR(64) NOT NULL COMMENT '题目编码',
  title VARCHAR(255) NOT NULL COMMENT '题目标题',
  question_type VARCHAR(32) NOT NULL COMMENT '题型 SINGLE/MULTI/TEXT/RATING/SCORE',
  options_json TEXT DEFAULT NULL COMMENT '选项JSON',
  required_flag TINYINT NOT NULL DEFAULT 0 COMMENT '是否必答',
  score_enabled TINYINT NOT NULL DEFAULT 0 COMMENT '是否计分',
  max_score INT DEFAULT NULL COMMENT '最高分',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
  sort_no INT NOT NULL DEFAULT 0 COMMENT '排序',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_survey_question_code (org_id, question_code),
  KEY idx_survey_question_tenant_id (tenant_id),
  KEY idx_survey_question_org_id (org_id),
  KEY idx_survey_question_type (question_type)
) COMMENT='问卷题库';

CREATE TABLE IF NOT EXISTS survey_template (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  template_code VARCHAR(64) NOT NULL COMMENT '模板编码',
  template_name VARCHAR(128) NOT NULL COMMENT '模板名称',
  description VARCHAR(255) DEFAULT NULL COMMENT '描述',
  target_type VARCHAR(32) NOT NULL COMMENT '对象类型 ELDER/STAFF/FAMILY/OTHER',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0草稿 1发布 2停用',
  start_date DATE DEFAULT NULL COMMENT '开始日期',
  end_date DATE DEFAULT NULL COMMENT '结束日期',
  anonymous_flag TINYINT NOT NULL DEFAULT 0 COMMENT '匿名 1是 0否',
  score_enabled TINYINT NOT NULL DEFAULT 0 COMMENT '是否计分',
  total_score INT DEFAULT NULL COMMENT '总分',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_survey_template_code (org_id, template_code),
  KEY idx_survey_template_tenant_id (tenant_id),
  KEY idx_survey_template_org_id (org_id),
  KEY idx_survey_template_target (target_type),
  KEY idx_survey_template_status (status)
) COMMENT='问卷模板';

CREATE TABLE IF NOT EXISTS survey_template_question (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  template_id BIGINT NOT NULL COMMENT '模板ID',
  question_id BIGINT NOT NULL COMMENT '题目ID',
  sort_no INT NOT NULL DEFAULT 0 COMMENT '排序',
  required_flag TINYINT NOT NULL DEFAULT 0 COMMENT '是否必答',
  weight DECIMAL(8,2) DEFAULT NULL COMMENT '权重',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_survey_template_question (org_id, template_id, question_id),
  KEY idx_survey_template_question_tenant_id (tenant_id),
  KEY idx_survey_template_id (template_id),
  KEY idx_survey_question_id (question_id)
) COMMENT='问卷模板-题目';

CREATE TABLE IF NOT EXISTS survey_submission (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  template_id BIGINT NOT NULL COMMENT '模板ID',
  target_type VARCHAR(32) NOT NULL COMMENT '对象类型',
  target_id BIGINT DEFAULT NULL COMMENT '对象ID',
  related_staff_id BIGINT DEFAULT NULL COMMENT '关联员工ID',
  submitter_id BIGINT DEFAULT NULL COMMENT '提交人ID',
  submitter_name VARCHAR(64) DEFAULT NULL COMMENT '提交人名称',
  submitter_role VARCHAR(32) DEFAULT NULL COMMENT '提交人角色',
  anonymous_flag TINYINT NOT NULL DEFAULT 0 COMMENT '匿名 1是 0否',
  score_total INT DEFAULT NULL COMMENT '总分',
  score_effective INT DEFAULT NULL COMMENT '有效分',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1已提交 0草稿',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_survey_submission_tenant_id (tenant_id),
  KEY idx_survey_submission_org (org_id),
  KEY idx_survey_submission_template (template_id),
  KEY idx_survey_submission_target (target_type, target_id),
  KEY idx_survey_submission_staff (related_staff_id)
) COMMENT='问卷提交';

CREATE TABLE IF NOT EXISTS survey_submission_item (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  submission_id BIGINT NOT NULL COMMENT '提交ID',
  question_id BIGINT NOT NULL COMMENT '题目ID',
  answer_json TEXT DEFAULT NULL COMMENT '答案JSON',
  answer_text TEXT DEFAULT NULL COMMENT '答案文本',
  score INT DEFAULT NULL COMMENT '得分',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  KEY idx_survey_item_tenant_id (tenant_id),
  KEY idx_survey_item_submission (submission_id),
  KEY idx_survey_item_question (question_id)
) COMMENT='问卷提交明细';

UPDATE survey_question_bank SET tenant_id = org_id WHERE tenant_id = 0;
UPDATE survey_template SET tenant_id = org_id WHERE tenant_id = 0;
UPDATE survey_template_question SET tenant_id = org_id WHERE tenant_id = 0;
UPDATE survey_submission SET tenant_id = org_id WHERE tenant_id = 0;
UPDATE survey_submission_item SET tenant_id = org_id WHERE tenant_id = 0;
