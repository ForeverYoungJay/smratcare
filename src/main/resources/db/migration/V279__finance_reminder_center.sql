-- 自动提醒：统一提醒表 + 幂等去重键
-- 提醒类型：NEXT_MONTH_CARE_FEE 下月代养费 / DEPOSIT_SHORTFALL 押金未缴清
--          ELECTRICITY_UNRECORDED 电费未登记 / ELECTRICITY_UNPAID 电费未缴

CREATE TABLE IF NOT EXISTS finance_reminder (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NULL,
  org_id BIGINT NOT NULL,
  reminder_type VARCHAR(40) NOT NULL COMMENT '提醒类型',
  /** 幂等键：同类型同周期同对象只生成一条，例如 NEXT_MONTH_CARE_FEE:2026-09 */
  dedupe_key VARCHAR(160) NOT NULL,
  title VARCHAR(200) NOT NULL,
  content VARCHAR(1000) NULL,
  severity VARCHAR(16) NOT NULL DEFAULT 'INFO' COMMENT 'INFO / WARNING / DANGER',
  biz_month VARCHAR(7) NULL COMMENT '关联账期 yyyy-MM',
  elder_id BIGINT NULL,
  room_id BIGINT NULL,
  amount DECIMAL(12,2) NULL,
  action_path VARCHAR(200) NULL COMMENT '前端跳转路径',
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING 未处理 / HANDLED 已处理',
  handled_at DATETIME NULL,
  handled_by BIGINT NULL,
  handle_remark VARCHAR(200) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_finance_reminder_dedupe (org_id, dedupe_key, is_deleted),
  KEY idx_finance_reminder_status (org_id, status, reminder_type),
  KEY idx_finance_reminder_created (org_id, create_time)
) COMMENT='财务提醒';
