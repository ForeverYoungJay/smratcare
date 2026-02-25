-- M6 积分规则配置

CREATE TABLE IF NOT EXISTS staff_points_rule (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  template_id BIGINT DEFAULT NULL COMMENT '护理模板ID(为空表示默认规则)',
  base_points INT NOT NULL DEFAULT 1 COMMENT '完成基础积分',
  score_weight DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT '评分权重(每1分系数)',
  suspicious_penalty INT NOT NULL DEFAULT 0 COMMENT '可疑任务扣减(可为负)',
  fail_points INT NOT NULL DEFAULT 0 COMMENT '失败任务积分(可为负)',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  UNIQUE KEY uk_staff_points_rule_org_template (org_id, template_id),
  KEY idx_staff_points_rule_org (org_id),
  KEY idx_staff_points_rule_template (template_id)
) COMMENT='员工积分规则';
