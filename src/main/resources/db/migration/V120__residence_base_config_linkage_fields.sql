SET @add_building_area_code_sql = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.columns
      WHERE table_schema = DATABASE()
        AND table_name = 'building'
        AND column_name = 'area_code'
    ),
    'SELECT 1',
    'ALTER TABLE building ADD COLUMN area_code VARCHAR(64) DEFAULT NULL COMMENT ''区域编码'' AFTER code'
  )
);
PREPARE stmt FROM @add_building_area_code_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_building_area_name_sql = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.columns
      WHERE table_schema = DATABASE()
        AND table_name = 'building'
        AND column_name = 'area_name'
    ),
    'SELECT 1',
    'ALTER TABLE building ADD COLUMN area_name VARCHAR(128) DEFAULT NULL COMMENT ''区域名称'' AFTER area_code'
  )
);
PREPARE stmt FROM @add_building_area_name_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_bed_type_sql = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.columns
      WHERE table_schema = DATABASE()
        AND table_name = 'bed'
        AND column_name = 'bed_type'
    ),
    'SELECT 1',
    'ALTER TABLE bed ADD COLUMN bed_type VARCHAR(64) DEFAULT NULL COMMENT ''床位类型编码'' AFTER bed_no'
  )
);
PREPARE stmt FROM @add_bed_type_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @create_idx_building_sql = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.statistics
      WHERE table_schema = DATABASE()
        AND table_name = 'building'
        AND index_name = 'idx_building_tenant_area_code'
    ),
    'SELECT 1',
    'CREATE INDEX idx_building_tenant_area_code ON building (tenant_id, area_code)'
  )
);
PREPARE stmt FROM @create_idx_building_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @create_idx_bed_sql = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.statistics
      WHERE table_schema = DATABASE()
        AND table_name = 'bed'
        AND index_name = 'idx_bed_tenant_bed_type'
    ),
    'SELECT 1',
    'CREATE INDEX idx_bed_tenant_bed_type ON bed (tenant_id, bed_type)'
  )
);
PREPARE stmt FROM @create_idx_bed_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
