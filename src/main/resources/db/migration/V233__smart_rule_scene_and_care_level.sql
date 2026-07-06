-- IoT 安全预警闭环：场景规则扩展（护理等级适用范围 + 标准场景枚举归一），幂等

SET @smart_rule_care_scope_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'smart_alert_rule'
    AND COLUMN_NAME = 'care_level_scope'
);
SET @ddl = IF(
  @smart_rule_care_scope_exists = 0,
  'ALTER TABLE smart_alert_rule ADD COLUMN care_level_scope VARCHAR(64) DEFAULT NULL COMMENT ''适用护理等级，逗号分隔，空=全部'' AFTER disability_level_scope',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 标准场景类型枚举归一：FALL / SOS / BED_EXIT_TIMEOUT / GEO_FENCE / LINGER / VITAL
-- 仅更新内置全局规则（org_id IS NULL），旧枚举在服务端有别名兼容（BED_EXIT/LEAVE/STAY 等）
UPDATE smart_alert_rule SET event_type = 'BED_EXIT_TIMEOUT'
WHERE id = 3 AND org_id IS NULL AND event_type = 'BED_EXIT';

UPDATE smart_alert_rule SET event_type = 'GEO_FENCE'
WHERE id = 4 AND org_id IS NULL AND event_type = 'LEAVE';

UPDATE smart_alert_rule SET event_type = 'LINGER'
WHERE id = 5 AND org_id IS NULL AND event_type = 'STAY';
