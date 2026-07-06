-- 合规与数据安全：机构级安全策略（2FA / 密码有效期 / 会话超时 / 登录失败锁定 / 脱敏开关 / 日志留存）
CREATE TABLE IF NOT EXISTS compliance_security_policy (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NULL,
  org_id BIGINT NOT NULL,
  two_factor_enabled TINYINT NOT NULL DEFAULT 0,          -- 管理端双因子登录开关
  two_factor_roles VARCHAR(500) NULL,                      -- 需要 2FA 的角色（逗号分隔，空=开启时对全部角色生效）
  password_max_days INT NOT NULL DEFAULT 0,                -- 密码有效期天数（0=不启用）
  login_fail_lock_count INT NOT NULL DEFAULT 5,            -- 连续登录失败锁定次数（0=不启用）
  login_fail_lock_minutes INT NOT NULL DEFAULT 30,         -- 锁定时长（分钟）
  session_timeout_minutes INT NOT NULL DEFAULT 0,          -- 会话超时分钟（0=使用全局 JWT 默认）
  masking_enabled TINYINT NOT NULL DEFAULT 0,              -- 敏感字段脱敏展示开关
  masking_exempt_roles VARCHAR(500) NOT NULL DEFAULT 'SYS_ADMIN,ADMIN,DIRECTOR', -- 脱敏豁免角色（可看全文）
  log_retention_days INT NOT NULL DEFAULT 400,             -- 日志留存天数（等保2.0 要求 >= 180，任务侧硬编码下限）
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_csp_org (org_id, is_deleted)
);
