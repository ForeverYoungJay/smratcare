-- 行政管理：家属逐项授权与有效期/撤销、一卡通生命周期与新旧卡关联、任务待验收、
-- 公告接收对象与阅读回执、制度文档版本审核与到期复核

-- 家属授权：auth_scopes 为空表示历史绑定（沿用原全量可见）；撤销 = 软删除并记录撤销人和原因
ALTER TABLE elder_family
  ADD COLUMN auth_scopes VARCHAR(128) NULL COMMENT '授权范围 HEALTH,BILLING,PHOTO,PROXY,EMERGENCY；空=历史绑定全量',
  ADD COLUMN valid_until DATE NULL COMMENT '授权有效期（含当天），空=长期',
  ADD COLUMN authorized_by_name VARCHAR(64) NULL COMMENT '核验授权人',
  ADD COLUMN authorized_at DATETIME NULL,
  ADD COLUMN revoked_by_name VARCHAR(64) NULL,
  ADD COLUMN revoked_at DATETIME NULL,
  ADD COLUMN revoke_reason VARCHAR(255) NULL;

-- 一卡通：补卡后同一长者会有新旧两张卡，去掉“一人一卡”唯一约束，改由业务校验“一人仅一张在用卡”
ALTER TABLE one_card_account DROP INDEX uk_one_card_org_elder;
ALTER TABLE one_card_account
  ADD COLUMN prev_card_id BIGINT NULL COMMENT '补卡前的旧卡',
  ADD COLUMN status_reason VARCHAR(255) NULL COMMENT '最近一次状态变更原因',
  ADD COLUMN status_changed_at DATETIME NULL,
  ADD KEY idx_one_card_org_elder (org_id, elder_id);

-- 公告接收对象：历史公告保持对员工和家属均可见
ALTER TABLE oa_notice
  ADD COLUMN audience VARCHAR(16) NOT NULL DEFAULT 'ALL' COMMENT 'STAFF/FAMILY/ALL';

CREATE TABLE IF NOT EXISTS oa_notice_read (
  id BIGINT NOT NULL PRIMARY KEY,
  org_id BIGINT NULL,
  notice_id BIGINT NOT NULL,
  staff_id BIGINT NOT NULL,
  staff_name VARCHAR(64) NULL,
  read_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_oa_notice_read (notice_id, staff_id)
) COMMENT='员工公告阅读回执';

-- 制度文档：新版本不覆盖旧版本，审核通过后才成为当前版本
ALTER TABLE oa_document
  ADD COLUMN version_no INT NOT NULL DEFAULT 1,
  ADD COLUMN prev_version_id BIGINT NULL,
  ADD COLUMN is_current TINYINT NOT NULL DEFAULT 1,
  ADD COLUMN review_status VARCHAR(16) NOT NULL DEFAULT 'APPROVED' COMMENT 'PENDING/APPROVED/REJECTED',
  ADD COLUMN reviewer_name VARCHAR(64) NULL,
  ADD COLUMN reviewed_at DATETIME NULL,
  ADD COLUMN review_note VARCHAR(255) NULL,
  ADD COLUMN review_due_date DATE NULL COMMENT '下次复核日期';
