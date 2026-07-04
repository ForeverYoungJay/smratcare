-- 长护险护理服务包（items_json 描述服务项目与频次；price 单位：分）
CREATE TABLE IF NOT EXISTS ltci_service_package (
  id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NULL,
  org_id BIGINT NULL,
  package_code VARCHAR(64) NOT NULL,
  package_name VARCHAR(150) NOT NULL,
  level_scope VARCHAR(64) NULL,
  items_json JSON NULL,
  price BIGINT NOT NULL DEFAULT 0,
  fund_covered TINYINT NOT NULL DEFAULT 1,
  status TINYINT NOT NULL DEFAULT 1,
  remark VARCHAR(500) NULL,
  created_by BIGINT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_ltci_pkg_code (org_id, package_code, is_deleted),
  KEY idx_ltci_pkg_status (status)
);

-- 内置示例服务包（org_id 为 NULL，全局默认；机构可复制定制）
INSERT INTO ltci_service_package (id, org_id, package_code, package_name, level_scope, items_json, price, fund_covered, status, remark)
SELECT 1, NULL, 'PKG_MODERATE', '中度失能基础护理包', '2,3',
  CAST('[{"code":"clean","name":"清洁照护","freqPerWeek":7},{"code":"feed","name":"协助进食","freqPerWeek":21},{"code":"turn","name":"翻身拍背","freqPerWeek":14}]' AS JSON),
  6000, 1, 1, '示例包，机构须按当地标准复核'
WHERE NOT EXISTS (SELECT 1 FROM ltci_service_package WHERE id = 1);

INSERT INTO ltci_service_package (id, org_id, package_code, package_name, level_scope, items_json, price, fund_covered, status, remark)
SELECT 2, NULL, 'PKG_SEVERE', '重度失能强化护理包', '4,5',
  CAST('[{"code":"clean","name":"清洁照护","freqPerWeek":14},{"code":"feed","name":"协助进食","freqPerWeek":21},{"code":"turn","name":"翻身拍背","freqPerWeek":28},{"code":"pressure","name":"压疮预防","freqPerWeek":14}]' AS JSON),
  12000, 1, 1, '示例包，机构须按当地标准复核'
WHERE NOT EXISTS (SELECT 1 FROM ltci_service_package WHERE id = 2);
