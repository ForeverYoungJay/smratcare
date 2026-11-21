ALTER TABLE care_task_template ADD COLUMN IF NOT EXISTS service_items CLOB;
ALTER TABLE care_task_execute_log ALTER COLUMN remark VARCHAR(1000);
