-- 协同消息：服务端会话、成员（已读游标）、消息（客户端消息 ID 幂等）、消息转待办关联（任务仍是 oa_task）
CREATE TABLE IF NOT EXISTS chat_conversation (
  id BIGINT NOT NULL PRIMARY KEY,
  org_id BIGINT NOT NULL,
  conv_type VARCHAR(16) NOT NULL COMMENT 'DIRECT 单聊 / GROUP 群聊',
  title VARCHAR(100) NULL,
  department_id BIGINT NULL,
  direct_key VARCHAR(64) NULL COMMENT '单聊双方 ID 排序拼接，防止重复建单聊',
  created_by BIGINT NOT NULL,
  last_message_id BIGINT NULL,
  last_message_preview VARCHAR(200) NULL,
  last_message_at DATETIME NULL,
  archived_at DATETIME NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_chat_direct (org_id, direct_key)
) COMMENT='协同消息会话';

CREATE TABLE IF NOT EXISTS chat_member (
  id BIGINT NOT NULL PRIMARY KEY,
  conversation_id BIGINT NOT NULL,
  staff_id BIGINT NOT NULL,
  member_role VARCHAR(16) NOT NULL DEFAULT 'MEMBER' COMMENT 'OWNER/ADMIN/MEMBER',
  joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  left_at DATETIME NULL COMMENT '退出或被移出后立即失去访问权',
  muted TINYINT NOT NULL DEFAULT 0,
  last_read_message_id BIGINT NULL,
  UNIQUE KEY uk_chat_member (conversation_id, staff_id),
  KEY idx_chat_member_staff (staff_id, left_at)
) COMMENT='会话成员与已读游标';

CREATE TABLE IF NOT EXISTS chat_message (
  id BIGINT NOT NULL PRIMARY KEY,
  conversation_id BIGINT NOT NULL,
  sender_id BIGINT NOT NULL,
  sender_name VARCHAR(64) NULL,
  message_type VARCHAR(16) NOT NULL DEFAULT 'TEXT' COMMENT 'TEXT/SYSTEM/TASK_CARD',
  body VARCHAR(2000) NULL,
  reply_to_id BIGINT NULL,
  task_id BIGINT NULL,
  client_message_id VARCHAR(64) NULL,
  recalled_at DATETIME NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_chat_message_client (sender_id, client_message_id),
  KEY idx_chat_message_conv (conversation_id, id)
) COMMENT='协同消息';

CREATE TABLE IF NOT EXISTS chat_task_link (
  id BIGINT NOT NULL PRIMARY KEY,
  org_id BIGINT NOT NULL,
  task_id BIGINT NOT NULL,
  conversation_id BIGINT NOT NULL,
  message_id BIGINT NOT NULL,
  created_by BIGINT NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_chat_task_link (message_id, task_id),
  KEY idx_chat_task_link_task (task_id)
) COMMENT='消息转待办：来源消息与统一任务（oa_task）的关联';

-- 年度假期额度（人事维护）；已用天数按已批准的请假审批实时计算，不另存
CREATE TABLE IF NOT EXISTS hr_leave_quota (
  id BIGINT NOT NULL PRIMARY KEY,
  org_id BIGINT NOT NULL,
  staff_id BIGINT NOT NULL,
  quota_year INT NOT NULL,
  leave_type VARCHAR(32) NOT NULL COMMENT '与请假审批的假别一致，如 年假/调休',
  quota_days DECIMAL(6,1) NOT NULL DEFAULT 0,
  updated_by_name VARCHAR(64) NULL,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_hr_leave_quota (staff_id, quota_year, leave_type)
) COMMENT='员工年度假期额度';
