-- 修复：admin 默认部门应为行政人事部（HR），而非护理部
-- 兼容存量库：若 admin 所在机构不存在 HR 部门则自动创建，再回写 admin.department_id

SET @admin_org_id := (
    SELECT s.org_id
    FROM staff s
    WHERE s.username = 'admin' AND s.is_deleted = 0
    ORDER BY s.id
    LIMIT 1
);

SET @hr_dept_id := (
    SELECT d.id
    FROM department d
    WHERE d.org_id = @admin_org_id
      AND d.dept_code = 'HR'
      AND d.is_deleted = 0
    ORDER BY d.id
    LIMIT 1
);

INSERT INTO department (
    id, org_id, parent_id, dept_name, dept_code, sort_no, status, create_time, update_time, is_deleted
)
SELECT
    (SELECT COALESCE(MAX(d0.id), 0) + 1 FROM department d0),
    @admin_org_id,
    NULL,
    '行政人事部',
    'HR',
    99,
    1,
    NOW(),
    NOW(),
    0
FROM DUAL
WHERE @admin_org_id IS NOT NULL
  AND @hr_dept_id IS NULL;

SET @hr_dept_id := (
    SELECT d.id
    FROM department d
    WHERE d.org_id = @admin_org_id
      AND d.dept_code = 'HR'
      AND d.is_deleted = 0
    ORDER BY d.id
    LIMIT 1
);

UPDATE staff s
SET s.department_id = @hr_dept_id,
    s.update_time = NOW()
WHERE s.username = 'admin'
  AND s.org_id = @admin_org_id
  AND s.is_deleted = 0
  AND @hr_dept_id IS NOT NULL
  AND (s.department_id IS NULL OR s.department_id <> @hr_dept_id);
