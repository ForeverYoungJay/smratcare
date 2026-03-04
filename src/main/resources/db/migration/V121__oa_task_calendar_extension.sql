SET @add_calendar_type_sql = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.columns
      WHERE table_schema = DATABASE()
        AND table_name = 'oa_task'
        AND column_name = 'calendar_type'
    ),
    'SELECT 1',
    'ALTER TABLE oa_task ADD COLUMN calendar_type VARCHAR(32) NOT NULL DEFAULT ''WORK'' COMMENT ''日历种类 PERSONAL/WORK/DAILY/COLLAB'' AFTER assignee_name'
  )
);
PREPARE stmt FROM @add_calendar_type_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_plan_category_sql = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.columns
      WHERE table_schema = DATABASE()
        AND table_name = 'oa_task'
        AND column_name = 'plan_category'
    ),
    'SELECT 1',
    'ALTER TABLE oa_task ADD COLUMN plan_category VARCHAR(64) DEFAULT NULL COMMENT ''计划分类，如基础办公/行政日常'' AFTER calendar_type'
  )
);
PREPARE stmt FROM @add_plan_category_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_urgency_sql = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.columns
      WHERE table_schema = DATABASE()
        AND table_name = 'oa_task'
        AND column_name = 'urgency'
    ),
    'SELECT 1',
    'ALTER TABLE oa_task ADD COLUMN urgency VARCHAR(16) NOT NULL DEFAULT ''NORMAL'' COMMENT ''紧急程度 NORMAL/EMERGENCY'' AFTER plan_category'
  )
);
PREPARE stmt FROM @add_urgency_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_event_color_sql = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.columns
      WHERE table_schema = DATABASE()
        AND table_name = 'oa_task'
        AND column_name = 'event_color'
    ),
    'SELECT 1',
    'ALTER TABLE oa_task ADD COLUMN event_color VARCHAR(16) DEFAULT NULL COMMENT ''日历颜色'' AFTER urgency'
  )
);
PREPARE stmt FROM @add_event_color_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_collaborator_ids_sql = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.columns
      WHERE table_schema = DATABASE()
        AND table_name = 'oa_task'
        AND column_name = 'collaborator_ids'
    ),
    'SELECT 1',
    'ALTER TABLE oa_task ADD COLUMN collaborator_ids VARCHAR(512) DEFAULT NULL COMMENT ''协同成员ID，逗号分隔'' AFTER event_color'
  )
);
PREPARE stmt FROM @add_collaborator_ids_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_collaborator_names_sql = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.columns
      WHERE table_schema = DATABASE()
        AND table_name = 'oa_task'
        AND column_name = 'collaborator_names'
    ),
    'SELECT 1',
    'ALTER TABLE oa_task ADD COLUMN collaborator_names VARCHAR(512) DEFAULT NULL COMMENT ''协同成员姓名，逗号分隔'' AFTER collaborator_ids'
  )
);
PREPARE stmt FROM @add_collaborator_names_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_is_recurring_sql = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.columns
      WHERE table_schema = DATABASE()
        AND table_name = 'oa_task'
        AND column_name = 'is_recurring'
    ),
    'SELECT 1',
    'ALTER TABLE oa_task ADD COLUMN is_recurring TINYINT NOT NULL DEFAULT 0 COMMENT ''是否周期任务 0否 1是'' AFTER collaborator_names'
  )
);
PREPARE stmt FROM @add_is_recurring_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_recurrence_rule_sql = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.columns
      WHERE table_schema = DATABASE()
        AND table_name = 'oa_task'
        AND column_name = 'recurrence_rule'
    ),
    'SELECT 1',
    'ALTER TABLE oa_task ADD COLUMN recurrence_rule VARCHAR(16) DEFAULT NULL COMMENT ''重复频率 DAILY/WEEKLY/MONTHLY'' AFTER is_recurring'
  )
);
PREPARE stmt FROM @add_recurrence_rule_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_recurrence_interval_sql = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.columns
      WHERE table_schema = DATABASE()
        AND table_name = 'oa_task'
        AND column_name = 'recurrence_interval'
    ),
    'SELECT 1',
    'ALTER TABLE oa_task ADD COLUMN recurrence_interval INT DEFAULT NULL COMMENT ''重复间隔'' AFTER recurrence_rule'
  )
);
PREPARE stmt FROM @add_recurrence_interval_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_recurrence_count_sql = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.columns
      WHERE table_schema = DATABASE()
        AND table_name = 'oa_task'
        AND column_name = 'recurrence_count'
    ),
    'SELECT 1',
    'ALTER TABLE oa_task ADD COLUMN recurrence_count INT DEFAULT NULL COMMENT ''重复次数（含首项）'' AFTER recurrence_interval'
  )
);
PREPARE stmt FROM @add_recurrence_count_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_source_todo_id_sql = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.columns
      WHERE table_schema = DATABASE()
        AND table_name = 'oa_task'
        AND column_name = 'source_todo_id'
    ),
    'SELECT 1',
    'ALTER TABLE oa_task ADD COLUMN source_todo_id BIGINT DEFAULT NULL COMMENT ''来源待办ID'' AFTER recurrence_count'
  )
);
PREPARE stmt FROM @add_source_todo_id_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_calendar_type_sql = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.statistics
      WHERE table_schema = DATABASE()
        AND table_name = 'oa_task'
        AND index_name = 'idx_oa_task_org_calendar_type'
    ),
    'SELECT 1',
    'CREATE INDEX idx_oa_task_org_calendar_type ON oa_task (org_id, calendar_type)'
  )
);
PREPARE stmt FROM @idx_calendar_type_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_source_todo_sql = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.statistics
      WHERE table_schema = DATABASE()
        AND table_name = 'oa_task'
        AND index_name = 'idx_oa_task_source_todo'
    ),
    'SELECT 1',
    'CREATE INDEX idx_oa_task_source_todo ON oa_task (source_todo_id)'
  )
);
PREPARE stmt FROM @idx_source_todo_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
