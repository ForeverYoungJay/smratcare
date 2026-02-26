-- Schedule template recurrence + inventory adjustment type (idempotent)

SET @shift_template_recurrence_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'shift_template'
    AND COLUMN_NAME = 'recurrence_type'
);
SET @ddl = IF(
  @shift_template_recurrence_exists = 0,
  'ALTER TABLE shift_template ADD COLUMN recurrence_type VARCHAR(32) NOT NULL DEFAULT ''WEEKLY_ONCE'' COMMENT ''周期 DAILY_ONCE/WEEKLY_ONCE/WEEKLY_TWICE''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @shift_template_execute_staff_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'shift_template'
    AND COLUMN_NAME = 'execute_staff_id'
);
SET @ddl = IF(
  @shift_template_execute_staff_exists = 0,
  'ALTER TABLE shift_template ADD COLUMN execute_staff_id BIGINT DEFAULT NULL COMMENT ''执行人员工ID''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @shift_template_attendance_linked_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'shift_template'
    AND COLUMN_NAME = 'attendance_linked'
);
SET @ddl = IF(
  @shift_template_attendance_linked_exists = 0,
  'ALTER TABLE shift_template ADD COLUMN attendance_linked TINYINT NOT NULL DEFAULT 1 COMMENT ''是否联动打卡 0否 1是''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @inventory_adjustment_inventory_type_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'inventory_adjustment'
    AND COLUMN_NAME = 'inventory_type'
);
SET @ddl = IF(
  @inventory_adjustment_inventory_type_exists = 0,
  'ALTER TABLE inventory_adjustment ADD COLUMN inventory_type VARCHAR(32) DEFAULT NULL COMMENT ''盘点类型 ASSET/MATERIAL/CONSUMABLE''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

