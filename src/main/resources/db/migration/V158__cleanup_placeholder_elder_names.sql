-- Normalize historical placeholder names to avoid propagating "姓名待完善" into business pages.
SET @db_name = DATABASE();

SET @sql = (
  SELECT IF(
    COUNT(*) > 0,
    'UPDATE elder
       SET full_name = CASE
         WHEN COALESCE(NULLIF(TRIM(elder_code), ''''), '''') <> '''' THEN CONCAT(''长者'', TRIM(elder_code))
         ELSE CONCAT(''长者'', RIGHT(CAST(id AS CHAR), 6))
       END
     WHERE TRIM(full_name) IN (''姓名待完善'', ''未命名长者'')',
    'SELECT 1'
  )
  FROM information_schema.columns
  WHERE table_schema = @db_name
    AND table_name = 'elder'
    AND column_name = 'full_name'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    COUNT(*) > 0,
    'UPDATE crm_contract
       SET elder_name = NULL
     WHERE TRIM(elder_name) IN (''姓名待完善'', ''未命名长者'')',
    'SELECT 1'
  )
  FROM information_schema.columns
  WHERE table_schema = @db_name
    AND table_name = 'crm_contract'
    AND column_name = 'elder_name'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    COUNT(*) > 0,
    'UPDATE crm_lead
       SET elder_name = NULL
     WHERE TRIM(elder_name) IN (''姓名待完善'', ''未命名长者'')',
    'SELECT 1'
  )
  FROM information_schema.columns
  WHERE table_schema = @db_name
    AND table_name = 'crm_lead'
    AND column_name = 'elder_name'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
