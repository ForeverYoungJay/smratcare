SET @delivery_signed_at_exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'dining_delivery_record'
    AND COLUMN_NAME = 'signed_at'
);
SET @delivery_signoff_image_urls_exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'dining_delivery_record'
    AND COLUMN_NAME = 'signoff_image_urls'
);
SET @delivery_qr_scan_at_exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'dining_delivery_record'
    AND COLUMN_NAME = 'qr_scan_at'
);

SET @sql = IF(
  @delivery_signed_at_exists = 0,
  'ALTER TABLE dining_delivery_record ADD COLUMN signed_at DATETIME DEFAULT NULL COMMENT ''床头码签收时间'' AFTER delivered_at',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
  @delivery_signoff_image_urls_exists = 0,
  'ALTER TABLE dining_delivery_record ADD COLUMN signoff_image_urls TEXT DEFAULT NULL COMMENT ''签收图片地址，多行存储'' AFTER signed_at',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
  @delivery_qr_scan_at_exists = 0,
  'ALTER TABLE dining_delivery_record ADD COLUMN qr_scan_at DATETIME DEFAULT NULL COMMENT ''床头码扫码回传时间'' AFTER signoff_image_urls',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
