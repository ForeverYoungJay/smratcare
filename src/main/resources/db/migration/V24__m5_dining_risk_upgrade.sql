-- 餐饮风险能力增强：结构化标签、阈值配置、拦截日志、放行审批
ALTER TABLE dining_dish
  ADD COLUMN tag_ids VARCHAR(255) DEFAULT NULL COMMENT '结构化标签ID，逗号分隔' AFTER allergen_tags;

ALTER TABLE dining_meal_order
  ADD COLUMN override_id BIGINT DEFAULT NULL COMMENT '放行审批ID' AFTER delivery_area_name;

CREATE TABLE IF NOT EXISTS dining_risk_threshold_config (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  metric_code VARCHAR(32) NOT NULL COMMENT '指标编码 SBP_MAX/DBP_MAX/BMI_MAX',
  metric_name VARCHAR(64) NOT NULL COMMENT '指标名称',
  threshold_value DECIMAL(10,2) NOT NULL COMMENT '阈值',
  enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用 1是0否',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否1是',
  UNIQUE KEY uk_dining_risk_threshold_org_metric (org_id, metric_code),
  KEY idx_dining_risk_threshold_org (org_id)
) COMMENT='餐饮风险阈值配置';

CREATE TABLE IF NOT EXISTS dining_risk_intercept_log (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  elder_id BIGINT NOT NULL COMMENT '老人ID',
  elder_name VARCHAR(64) DEFAULT NULL COMMENT '老人姓名',
  dish_names VARCHAR(1024) NOT NULL COMMENT '菜品名称快照',
  order_context VARCHAR(32) NOT NULL COMMENT '触发场景 ORDER_CREATE/ORDER_UPDATE/RISK_CHECK',
  risk_detail VARCHAR(2048) NOT NULL COMMENT '风险明细JSON',
  created_by BIGINT DEFAULT NULL COMMENT '触发人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否1是',
  KEY idx_dining_risk_intercept_org_time (org_id, create_time),
  KEY idx_dining_risk_intercept_elder (elder_id)
) COMMENT='餐饮风险拦截日志';

CREATE TABLE IF NOT EXISTS dining_risk_override (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  elder_id BIGINT NOT NULL COMMENT '老人ID',
  elder_name VARCHAR(64) DEFAULT NULL COMMENT '老人姓名',
  dish_names VARCHAR(1024) NOT NULL COMMENT '菜品快照',
  risk_detail VARCHAR(2048) NOT NULL COMMENT '风险明细JSON',
  apply_reason VARCHAR(255) NOT NULL COMMENT '申请原因',
  apply_staff_id BIGINT DEFAULT NULL COMMENT '申请人',
  review_status VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT '审批状态 PENDING/APPROVED/REJECTED',
  review_staff_id BIGINT DEFAULT NULL COMMENT '审批人',
  review_remark VARCHAR(255) DEFAULT NULL COMMENT '审批备注',
  reviewed_at DATETIME DEFAULT NULL COMMENT '审批时间',
  effective_until DATETIME DEFAULT NULL COMMENT '放行截止时间',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否1是',
  KEY idx_dining_risk_override_org_status (org_id, review_status),
  KEY idx_dining_risk_override_elder (elder_id)
) COMMENT='餐饮风险放行审批';

INSERT INTO dining_risk_threshold_config
(id, tenant_id, org_id, metric_code, metric_name, threshold_value, enabled, remark, created_by)
SELECT 910001, 1, 1, 'SBP_MAX', '收缩压上限', 140.00, 1, '默认阈值', 1
WHERE NOT EXISTS (
  SELECT 1 FROM dining_risk_threshold_config WHERE org_id = 1 AND metric_code = 'SBP_MAX'
);

INSERT INTO dining_risk_threshold_config
(id, tenant_id, org_id, metric_code, metric_name, threshold_value, enabled, remark, created_by)
SELECT 910002, 1, 1, 'DBP_MAX', '舒张压上限', 90.00, 1, '默认阈值', 1
WHERE NOT EXISTS (
  SELECT 1 FROM dining_risk_threshold_config WHERE org_id = 1 AND metric_code = 'DBP_MAX'
);

INSERT INTO dining_risk_threshold_config
(id, tenant_id, org_id, metric_code, metric_name, threshold_value, enabled, remark, created_by)
SELECT 910003, 1, 1, 'BMI_MAX', 'BMI上限', 28.00, 1, '默认阈值', 1
WHERE NOT EXISTS (
  SELECT 1 FROM dining_risk_threshold_config WHERE org_id = 1 AND metric_code = 'BMI_MAX'
);
