CREATE TABLE IF NOT EXISTS oa_suggestion (
  id BIGINT PRIMARY KEY COMMENT '主键ID',
  tenant_id BIGINT DEFAULT NULL COMMENT '租户ID',
  org_id BIGINT NOT NULL COMMENT '机构ID',
  content VARCHAR(1000) NOT NULL COMMENT '谏言内容',
  proposer_name VARCHAR(64) DEFAULT NULL COMMENT '提交人',
  contact VARCHAR(64) DEFAULT NULL COMMENT '联系方式',
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT '状态 PENDING/ADOPTED/REJECTED',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除'
) COMMENT='OA 运行谏言';

CREATE INDEX idx_oa_suggestion_org ON oa_suggestion(org_id);
CREATE INDEX idx_oa_suggestion_status ON oa_suggestion(status);
CREATE INDEX idx_oa_suggestion_create_time ON oa_suggestion(create_time);
