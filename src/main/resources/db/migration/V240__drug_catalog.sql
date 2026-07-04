-- 药品目录（药事管理基础字典）
CREATE TABLE IF NOT EXISTS drug_catalog (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NULL,
  org_id BIGINT NULL,
  drug_code VARCHAR(64) NOT NULL,
  drug_name VARCHAR(180) NOT NULL,
  generic_name VARCHAR(180) NULL,
  spec VARCHAR(120) NULL,
  unit VARCHAR(32) NULL,
  dosage_form VARCHAR(64) NULL,
  manufacturer VARCHAR(180) NULL,
  category VARCHAR(64) NULL,
  control_level VARCHAR(24) NOT NULL DEFAULT 'NORMAL',
  is_high_risk TINYINT NOT NULL DEFAULT 0,
  safety_stock INT NOT NULL DEFAULT 0,
  price BIGINT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  remark VARCHAR(500) NULL,
  created_by BIGINT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_drug_code (org_id, drug_code, is_deleted),
  KEY idx_drug_name (org_id, drug_name),
  KEY idx_drug_category (org_id, category, status)
);

-- 内置示例药品（org_id 为 NULL 全局默认；control_level: NORMAL/PRESCRIPTION/PSYCHOTROPIC/NARCOTIC）
INSERT INTO drug_catalog (id, org_id, drug_code, drug_name, generic_name, spec, unit, dosage_form, category, control_level, is_high_risk, safety_stock, price, status, remark)
SELECT 1, NULL, 'AMLO5', '氨氯地平片', '苯磺酸氨氯地平', '5mg*7片', '盒', '片剂', '心血管', 'PRESCRIPTION', 0, 20, 2800, 1, '降压'
WHERE NOT EXISTS (SELECT 1 FROM drug_catalog WHERE id = 1);

INSERT INTO drug_catalog (id, org_id, drug_code, drug_name, generic_name, spec, unit, dosage_form, category, control_level, is_high_risk, safety_stock, price, status, remark)
SELECT 2, NULL, 'METF', '二甲双胍片', '盐酸二甲双胍', '0.5g*30片', '盒', '片剂', '内分泌', 'PRESCRIPTION', 0, 20, 1500, 1, '降糖'
WHERE NOT EXISTS (SELECT 1 FROM drug_catalog WHERE id = 2);

INSERT INTO drug_catalog (id, org_id, drug_code, drug_name, generic_name, spec, unit, dosage_form, category, control_level, is_high_risk, safety_stock, price, status, remark)
SELECT 3, NULL, 'INSU', '门冬胰岛素注射液', '门冬胰岛素', '300U*3ml', '支', '注射剂', '内分泌', 'PRESCRIPTION', 1, 10, 6800, 1, '高危药品，胰岛素'
WHERE NOT EXISTS (SELECT 1 FROM drug_catalog WHERE id = 3);
