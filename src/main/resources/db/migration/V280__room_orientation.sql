-- 楼栋平面图需要按朝向分列到走廊南北两侧，room 表补朝向字段（幂等）

SET @room_orientation_exists = (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'room' AND COLUMN_NAME = 'orientation'
);
SET @ddl = IF(@room_orientation_exists = 0,
  'ALTER TABLE room ADD COLUMN orientation VARCHAR(16) DEFAULT NULL COMMENT ''朝向 SOUTH 南向 / NORTH 北向 / EAST 东向 / WEST 西向''',
  'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
