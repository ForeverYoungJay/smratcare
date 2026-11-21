-- 电费管理（主库定义见 V277）

CREATE TABLE IF NOT EXISTS finance_electricity_price (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT DEFAULT NULL,
  org_id BIGINT NOT NULL,
  unit_price DECIMAL(10,4) NOT NULL,
  effective_from DATE NOT NULL,
  remark VARCHAR(200) DEFAULT NULL,
  created_by BIGINT DEFAULT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_elec_price_org_from
  ON finance_electricity_price (org_id, effective_from, is_deleted);

CREATE TABLE IF NOT EXISTS finance_electricity_reading (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT DEFAULT NULL,
  org_id BIGINT NOT NULL,
  bill_month VARCHAR(7) NOT NULL,
  room_id BIGINT NOT NULL,
  building VARCHAR(64) DEFAULT NULL,
  floor_no VARCHAR(32) DEFAULT NULL,
  room_no VARCHAR(32) NOT NULL,
  previous_reading DECIMAL(12,2) NOT NULL DEFAULT 0,
  current_reading DECIMAL(12,2) NOT NULL DEFAULT 0,
  usage_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  unit_price DECIMAL(10,4) NOT NULL DEFAULT 0,
  fee_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  share_mode VARCHAR(24) NOT NULL DEFAULT 'ROOM',
  resident_count INT NOT NULL DEFAULT 0,
  per_resident_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  pay_status VARCHAR(16) NOT NULL DEFAULT 'UNPAID',
  paid_at DATETIME DEFAULT NULL,
  paid_by BIGINT DEFAULT NULL,
  pay_remark VARCHAR(200) DEFAULT NULL,
  remark VARCHAR(200) DEFAULT NULL,
  created_by BIGINT DEFAULT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_elec_reading_month_room
  ON finance_electricity_reading (org_id, bill_month, room_id, is_deleted);
CREATE INDEX IF NOT EXISTS idx_elec_reading_month
  ON finance_electricity_reading (org_id, bill_month, pay_status);
