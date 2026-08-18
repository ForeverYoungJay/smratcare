-- 电费管理：电价配置 + 按房间按月抄表与电费
-- 用电量 = 本期读数 − 上期读数；电费 = 用电量 × 生效电价（保留 2 位）

CREATE TABLE IF NOT EXISTS finance_electricity_price (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NULL,
  org_id BIGINT NOT NULL,
  unit_price DECIMAL(10,4) NOT NULL COMMENT '电价，元/度',
  effective_from DATE NOT NULL COMMENT '生效日期',
  remark VARCHAR(200) NULL,
  created_by BIGINT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_elec_price_org_from (org_id, effective_from, is_deleted),
  KEY idx_elec_price_org (org_id, effective_from)
) COMMENT='电价配置';

CREATE TABLE IF NOT EXISTS finance_electricity_reading (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NULL,
  org_id BIGINT NOT NULL,
  bill_month VARCHAR(7) NOT NULL COMMENT '账期 yyyy-MM',
  room_id BIGINT NOT NULL,
  building VARCHAR(64) NULL,
  floor_no VARCHAR(32) NULL,
  room_no VARCHAR(32) NOT NULL,
  previous_reading DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '上期读数',
  current_reading DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '本期读数',
  usage_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '用电量，度',
  unit_price DECIMAL(10,4) NOT NULL DEFAULT 0 COMMENT '登记时电价快照',
  fee_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '电费金额',
  share_mode VARCHAR(24) NOT NULL DEFAULT 'ROOM' COMMENT 'ROOM 整间计费 / PER_RESIDENT 按在住人数均摊',
  resident_count INT NOT NULL DEFAULT 0 COMMENT '分摊人数快照',
  per_resident_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '人均金额',
  pay_status VARCHAR(16) NOT NULL DEFAULT 'UNPAID' COMMENT 'UNPAID 未缴 / PAID 已缴',
  paid_at DATETIME NULL,
  paid_by BIGINT NULL,
  pay_remark VARCHAR(200) NULL,
  remark VARCHAR(200) NULL,
  created_by BIGINT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_elec_reading_month_room (org_id, bill_month, room_id, is_deleted),
  KEY idx_elec_reading_month (org_id, bill_month, pay_status),
  KEY idx_elec_reading_room (org_id, room_no)
) COMMENT='房间月度电费';
