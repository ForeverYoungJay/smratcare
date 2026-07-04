-- H2 测试库此前没有任何迁移创建 oa_album，导致下方 CREATE INDEX 报 "Table not found"、
-- 集成测试上下文无法启动；这里按主库 V34__oa_album_knowledge_reward.sql 的结构先补建该表。
CREATE TABLE IF NOT EXISTS oa_album (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  org_id BIGINT NOT NULL,
  title VARCHAR(128) NOT NULL,
  category VARCHAR(64) DEFAULT NULL,
  cover_url VARCHAR(512) DEFAULT NULL,
  photo_count INT NOT NULL DEFAULT 0,
  shoot_date DATE DEFAULT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
  remark VARCHAR(255) DEFAULT NULL,
  created_by BIGINT DEFAULT NULL,
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

ALTER TABLE IF EXISTS oa_album ADD COLUMN IF NOT EXISTS album_scope VARCHAR(16) NOT NULL DEFAULT 'GROUP';
ALTER TABLE IF EXISTS oa_album ADD COLUMN IF NOT EXISTS elder_id BIGINT DEFAULT NULL;
CREATE INDEX IF NOT EXISTS idx_oa_album_org_scope_elder ON oa_album (org_id, album_scope, elder_id, status);
