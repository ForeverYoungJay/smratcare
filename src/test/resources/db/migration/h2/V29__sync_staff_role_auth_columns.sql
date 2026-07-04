-- 同步 H2 测试库 staff/role 表结构与实体字段（主库对应 V143/V177/V178/V179 等迁移），
-- 否则登录链路 select * 会报 Column not found，集成测试全部 500。
ALTER TABLE staff ADD COLUMN IF NOT EXISTS password_plaintext_snapshot VARCHAR(255) DEFAULT NULL;
ALTER TABLE staff ADD COLUMN IF NOT EXISTS password_snapshot_updated_at TIMESTAMP DEFAULT NULL;
ALTER TABLE staff ADD COLUMN IF NOT EXISTS direct_leader_id BIGINT DEFAULT NULL;
ALTER TABLE staff ADD COLUMN IF NOT EXISTS indirect_leader_id BIGINT DEFAULT NULL;

ALTER TABLE role ADD COLUMN IF NOT EXISTS department_id BIGINT DEFAULT NULL;
ALTER TABLE role ADD COLUMN IF NOT EXISTS superior_role_id BIGINT DEFAULT NULL;
ALTER TABLE role ADD COLUMN IF NOT EXISTS route_permissions_json CLOB;
