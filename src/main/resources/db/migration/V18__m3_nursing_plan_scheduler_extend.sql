-- M3 照护：服务计划自动生成预定扩展（幂等）

SET @plan_preferred_time_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'service_plan'
    AND COLUMN_NAME = 'preferred_time'
);
SET @ddl = IF(
  @plan_preferred_time_exists = 0,
  'ALTER TABLE service_plan ADD COLUMN preferred_time TIME DEFAULT ''09:00:00'' COMMENT ''默认预约时间'' AFTER end_date',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_service_plan_cycle_exists = (
  SELECT COUNT(1)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'service_plan'
    AND INDEX_NAME = 'idx_service_plan_cycle'
);
SET @ddl = IF(
  @idx_service_plan_cycle_exists = 0,
  'CREATE INDEX idx_service_plan_cycle ON service_plan (cycle_type, start_date, end_date)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_service_booking_plan_time_exists = (
  SELECT COUNT(1)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'service_booking'
    AND INDEX_NAME = 'idx_service_booking_plan_time'
);
SET @ddl = IF(
  @idx_service_booking_plan_time_exists = 0,
  'CREATE INDEX idx_service_booking_plan_time ON service_booking (plan_id, booking_time)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
