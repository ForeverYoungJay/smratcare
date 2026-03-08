CREATE TABLE IF NOT EXISTS oa_quick_chat_state (
  id BIGINT PRIMARY KEY,
  tenant_id BIGINT NULL,
  org_id BIGINT NULL,
  staff_id BIGINT NOT NULL,
  state_json LONGTEXT NOT NULL,
  created_by BIGINT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_oa_quick_chat_state_org_staff (org_id, staff_id),
  KEY idx_oa_quick_chat_state_staff (staff_id),
  KEY idx_oa_quick_chat_state_update (update_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
