SET @schema_name = DATABASE();
SET @has_room_table := (
  SELECT COUNT(1)
  FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = @schema_name
    AND TABLE_NAME = 'room'
);
SET @has_room_sort_no := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @schema_name
    AND TABLE_NAME = 'room'
    AND COLUMN_NAME = 'sort_no'
);
SET @ddl_add_room_sort_no := IF(
  @has_room_table = 1 AND @has_room_sort_no = 0,
  'ALTER TABLE room ADD COLUMN sort_no INT NOT NULL DEFAULT 0 COMMENT ''排序'' AFTER room_type',
  'SELECT 1'
);
PREPARE stmt_add_room_sort_no FROM @ddl_add_room_sort_no;
EXECUTE stmt_add_room_sort_no;
DEALLOCATE PREPARE stmt_add_room_sort_no;
