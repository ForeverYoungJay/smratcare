-- H2 版本：与 MySQL V297 一致
-- 协同消息：服务端会话、成员（已读游标）、消息（客户端消息 ID 幂等）、消息转待办关联（任务仍是 oa_task）
CREATE TABLE IF NOT EXISTS chat_conversation (
  id BIGINT NOT NULL PRIMARY KEY,
  org_id BIGINT NOT NULL,
  conv_type VARCHAR(16) NOT NULL,
  title VARCHAR(100) NULL,
  department_id BIGINT NULL,
  direct_key VARCHAR(64) NULL,
  created_by BIGINT NOT NULL,
  last_message_id BIGINT NULL,
  last_message_preview VARCHAR(200) NULL,
  last_message_at DATETIME NULL,
  archived_at DATETIME NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_chat_direct UNIQUE (org_id, direct_key)
);

CREATE TABLE IF NOT EXISTS chat_member (
  id BIGINT NOT NULL PRIMARY KEY,
  conversation_id BIGINT NOT NULL,
  staff_id BIGINT NOT NULL,
  member_role VARCHAR(16) NOT NULL DEFAULT 'MEMBER',
  joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  left_at DATETIME NULL,
  muted TINYINT NOT NULL DEFAULT 0,
  last_read_message_id BIGINT NULL,
  CONSTRAINT uk_chat_member UNIQUE (conversation_id, staff_id)
);

CREATE TABLE IF NOT EXISTS chat_message (
  id BIGINT NOT NULL PRIMARY KEY,
  conversation_id BIGINT NOT NULL,
  sender_id BIGINT NOT NULL,
  sender_name VARCHAR(64) NULL,
  message_type VARCHAR(16) NOT NULL DEFAULT 'TEXT',
  body VARCHAR(2000) NULL,
  reply_to_id BIGINT NULL,
  task_id BIGINT NULL,
  client_message_id VARCHAR(64) NULL,
  recalled_at DATETIME NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_chat_message_client UNIQUE (sender_id, client_message_id)
);

CREATE TABLE IF NOT EXISTS chat_task_link (
  id BIGINT NOT NULL PRIMARY KEY,
  org_id BIGINT NOT NULL,
  task_id BIGINT NOT NULL,
  conversation_id BIGINT NOT NULL,
  message_id BIGINT NOT NULL,
  created_by BIGINT NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_chat_task_link UNIQUE (message_id, task_id)
);

-- 年度假期额度（人事维护）；已用天数按已批准的请假审批实时计算，不另存
CREATE TABLE IF NOT EXISTS hr_leave_quota (
  id BIGINT NOT NULL PRIMARY KEY,
  org_id BIGINT NOT NULL,
  staff_id BIGINT NOT NULL,
  quota_year INT NOT NULL,
  leave_type VARCHAR(32) NOT NULL,
  quota_days DECIMAL(6,1) NOT NULL DEFAULT 0,
  updated_by_name VARCHAR(64) NULL,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_hr_leave_quota UNIQUE (staff_id, quota_year, leave_type)
);
