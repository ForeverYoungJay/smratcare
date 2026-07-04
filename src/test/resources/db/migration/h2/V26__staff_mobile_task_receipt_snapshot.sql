ALTER TABLE operations_staff_task_receipt ADD COLUMN IF NOT EXISTS task_title VARCHAR(160);
ALTER TABLE operations_staff_task_receipt ADD COLUMN IF NOT EXISTS resident VARCHAR(80);
ALTER TABLE operations_staff_task_receipt ADD COLUMN IF NOT EXISTS room VARCHAR(80);
