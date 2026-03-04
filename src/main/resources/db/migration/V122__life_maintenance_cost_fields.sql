SET @add_labor_cost_sql = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.columns
      WHERE table_schema = DATABASE()
        AND table_name = 'life_maintenance_request'
        AND column_name = 'labor_cost'
    ),
    'SELECT 1',
    'ALTER TABLE life_maintenance_request ADD COLUMN labor_cost DECIMAL(12,2) DEFAULT 0.00 COMMENT ''人工成本'' AFTER description'
  )
);
PREPARE stmt FROM @add_labor_cost_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_material_cost_sql = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.columns
      WHERE table_schema = DATABASE()
        AND table_name = 'life_maintenance_request'
        AND column_name = 'material_cost'
    ),
    'SELECT 1',
    'ALTER TABLE life_maintenance_request ADD COLUMN material_cost DECIMAL(12,2) DEFAULT 0.00 COMMENT ''物料成本'' AFTER labor_cost'
  )
);
PREPARE stmt FROM @add_material_cost_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_total_cost_sql = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.columns
      WHERE table_schema = DATABASE()
        AND table_name = 'life_maintenance_request'
        AND column_name = 'total_cost'
    ),
    'SELECT 1',
    'ALTER TABLE life_maintenance_request ADD COLUMN total_cost DECIMAL(12,2) DEFAULT 0.00 COMMENT ''总成本'' AFTER material_cost'
  )
);
PREPARE stmt FROM @add_total_cost_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_reported_at_sql = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.statistics
      WHERE table_schema = DATABASE()
        AND table_name = 'life_maintenance_request'
        AND index_name = 'idx_life_maintenance_org_reported_at'
    ),
    'SELECT 1',
    'CREATE INDEX idx_life_maintenance_org_reported_at ON life_maintenance_request (org_id, reported_at)'
  )
);
PREPARE stmt FROM @idx_reported_at_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
