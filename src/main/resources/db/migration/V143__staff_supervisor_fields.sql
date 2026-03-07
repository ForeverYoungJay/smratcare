-- 员工监管链：直接领导 / 间接领导

SET @has_direct_leader_id := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'staff'
    AND COLUMN_NAME = 'direct_leader_id'
);
SET @sql_direct_leader_id := IF(
  @has_direct_leader_id = 0,
  'ALTER TABLE staff ADD COLUMN direct_leader_id BIGINT DEFAULT NULL COMMENT ''直接领导ID'' AFTER email',
  'SELECT 1'
);
PREPARE stmt FROM @sql_direct_leader_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_indirect_leader_id := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'staff'
    AND COLUMN_NAME = 'indirect_leader_id'
);
SET @sql_indirect_leader_id := IF(
  @has_indirect_leader_id = 0,
  'ALTER TABLE staff ADD COLUMN indirect_leader_id BIGINT DEFAULT NULL COMMENT ''间接领导ID'' AFTER direct_leader_id',
  'SELECT 1'
);
PREPARE stmt FROM @sql_indirect_leader_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_staff_direct_leader := (
  SELECT COUNT(1)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'staff'
    AND INDEX_NAME = 'idx_staff_direct_leader'
);
SET @sql_idx_staff_direct_leader := IF(
  @idx_staff_direct_leader = 0,
  'CREATE INDEX idx_staff_direct_leader ON staff (org_id, direct_leader_id)',
  'SELECT 1'
);
PREPARE stmt FROM @sql_idx_staff_direct_leader;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_staff_indirect_leader := (
  SELECT COUNT(1)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'staff'
    AND INDEX_NAME = 'idx_staff_indirect_leader'
);
SET @sql_idx_staff_indirect_leader := IF(
  @idx_staff_indirect_leader = 0,
  'CREATE INDEX idx_staff_indirect_leader ON staff (org_id, indirect_leader_id)',
  'SELECT 1'
);
PREPARE stmt FROM @sql_idx_staff_indirect_leader;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
