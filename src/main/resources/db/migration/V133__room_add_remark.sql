SET @schema_name = DATABASE();
SET @has_room_table := (
  SELECT COUNT(1)
  FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = @schema_name
    AND TABLE_NAME = 'room'
);
SET @has_room_remark := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @schema_name
    AND TABLE_NAME = 'room'
    AND COLUMN_NAME = 'remark'
);
SET @ddl_add_room_remark := IF(
  @has_room_table = 1 AND @has_room_remark = 0,
  'ALTER TABLE room ADD COLUMN remark VARCHAR(1000) NULL COMMENT ''备注（支持JSON扩展）'' AFTER room_qr_code',
  'SELECT 1'
);
PREPARE stmt_add_room_remark FROM @ddl_add_room_remark;
EXECUTE stmt_add_room_remark;
DEALLOCATE PREPARE stmt_add_room_remark;
