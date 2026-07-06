-- 合规与数据安全：登录留痕（成功/失败均记录，支撑失败锁定与等保审计）
CREATE TABLE IF NOT EXISTS auth_login_log (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NULL,
  org_id BIGINT NULL,
  staff_id BIGINT NULL,
  username VARCHAR(120) NOT NULL,
  login_type VARCHAR(32) NOT NULL DEFAULT 'PASSWORD',  -- PASSWORD / 2FA_CHALLENGE / 2FA_VERIFY
  result VARCHAR(16) NOT NULL,                          -- SUCCESS / FAIL
  fail_reason VARCHAR(255) NULL,                        -- BAD_CREDENTIALS / LOCKED / PASSWORD_EXPIRED / 2FA_CODE_ERROR ...
  ip VARCHAR(64) NULL,
  user_agent VARCHAR(255) NULL,
  request_id VARCHAR(64) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_all_username_time (username, create_time),
  KEY idx_all_org_time (org_id, create_time)
);
