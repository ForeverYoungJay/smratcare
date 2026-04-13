-- 创建平台构建者专属 SYS_ADMIN 根账号
-- 设计口径：
-- 1. admin 是业务管理员，仅持有 ADMIN
-- 2. SYS_ADMIN 是平台构建者专属隐藏根账号

SET @sys_admin_org_id := COALESCE(
    (
        SELECT s.org_id
        FROM staff s
        WHERE s.username = 'admin'
          AND s.is_deleted = 0
        ORDER BY s.id
        LIMIT 1
    ),
    (
        SELECT o.id
        FROM org o
        WHERE o.is_deleted = 0
        ORDER BY o.id
        LIMIT 1
    )
);

SET @sys_admin_dept_id := COALESCE(
    (
        SELECT d.id
        FROM department d
        WHERE d.org_id = @sys_admin_org_id
          AND d.dept_code = 'HR'
          AND d.is_deleted = 0
        ORDER BY d.id
        LIMIT 1
    ),
    (
        SELECT s.department_id
        FROM staff s
        WHERE s.org_id = @sys_admin_org_id
          AND s.username = 'admin'
          AND s.is_deleted = 0
        ORDER BY s.id
        LIMIT 1
    ),
    (
        SELECT d.id
        FROM department d
        WHERE d.org_id = @sys_admin_org_id
          AND d.is_deleted = 0
        ORDER BY d.id
        LIMIT 1
    )
);

INSERT INTO staff (
    id,
    org_id,
    department_id,
    staff_no,
    username,
    password_hash,
    password_plaintext_snapshot,
    password_snapshot_updated_at,
    real_name,
    phone,
    email,
    gender,
    status,
    last_login_time,
    create_time,
    update_time,
    is_deleted
)
SELECT
    UUID_SHORT(),
    @sys_admin_org_id,
    @sys_admin_dept_id,
    'SYS-ROOT-001',
    'sysadmin_root',
    '$2y$10$tihkpKP6daXo/c1Fw0DT6.DisNV6JP5awJCbZARHUEusfssntySBW',
    'SysAdmin@2026!Care',
    NOW(),
    '平台构建者',
    NULL,
    NULL,
    1,
    1,
    NULL,
    NOW(),
    NOW(),
    0
FROM DUAL
WHERE @sys_admin_org_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM staff s
    WHERE s.org_id = @sys_admin_org_id
      AND s.username = 'sysadmin_root'
      AND s.is_deleted = 0
  );

INSERT INTO staff_role (id, org_id, staff_id, role_id, create_time, update_time, is_deleted)
SELECT
    UUID_SHORT(),
    s.org_id,
    s.id,
    r.id,
    NOW(),
    NOW(),
    0
FROM staff s
JOIN role r ON r.org_id = s.org_id
WHERE s.org_id = @sys_admin_org_id
  AND s.username = 'sysadmin_root'
  AND s.is_deleted = 0
  AND r.role_code = 'SYS_ADMIN'
  AND r.is_deleted = 0
  AND NOT EXISTS (
    SELECT 1
    FROM staff_role sr
    WHERE sr.org_id = s.org_id
      AND sr.staff_id = s.id
      AND sr.role_id = r.id
      AND sr.is_deleted = 0
  );
