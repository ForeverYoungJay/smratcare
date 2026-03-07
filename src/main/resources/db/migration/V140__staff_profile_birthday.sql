SET @has_birthday = (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'staff_profile' AND column_name = 'birthday'
);
SET @sql_birthday = IF(
  @has_birthday = 0,
  'ALTER TABLE staff_profile ADD COLUMN birthday DATE NULL COMMENT ''生日'' AFTER emergency_contact_phone',
  'SELECT 1'
);
PREPARE stmt_birthday FROM @sql_birthday;
EXECUTE stmt_birthday;
DEALLOCATE PREPARE stmt_birthday;
