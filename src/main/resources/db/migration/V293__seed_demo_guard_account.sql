-- 示例机构补一个保卫（GUARD）演示账号 guard_demo，默认演示密码与其他演示账号相同；仅在 demo_seed_enabled 时生效
SET @demo_seed_on := IF(LOWER('${demo_seed_enabled}') IN ('1', 'true', 'yes', 'on'), 1, 0);
SET @demo_org_id := (
  IF(@demo_seed_on = 1,
    (SELECT o.id FROM org o WHERE o.is_deleted = 0 AND (o.org_code = 'YY-GF-001' OR o.org_name = '弋阳龟峰颐养中心') ORDER BY o.id LIMIT 1),
    NULL)
);

INSERT INTO role (id, org_id, role_name, role_code, role_desc, status, create_time, update_time, is_deleted)
SELECT UUID_SHORT(), @demo_org_id, '保卫/消防巡查', 'GUARD', '防火巡查、控制室值班', 1, NOW(), NOW(), 0
FROM DUAL
WHERE @demo_org_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM role r WHERE r.org_id = @demo_org_id AND r.role_code = 'GUARD' AND r.is_deleted = 0);

INSERT INTO staff (id, org_id, department_id, staff_no, username, password_hash, real_name, phone, email, gender, status, last_login_time, create_time, update_time, is_deleted)
SELECT 15304, @demo_org_id, d.id, 'DEMO-1304', 'guard_demo', '$2a$10$A1rIAhc8BXcMXR/imDcBMeabjSM2/2z4pIjpGYBnMJ8Z.C8Z1e/Ym',
       '保卫巡查员何', '13000001304', NULL, 1, 1, NULL, NOW(), NOW(), 0
FROM department d
WHERE @demo_org_id IS NOT NULL
  AND d.org_id = @demo_org_id AND d.dept_code = 'LOGI' AND d.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM staff s WHERE s.id = 15304 OR (s.org_id = @demo_org_id AND s.username = 'guard_demo' AND s.is_deleted = 0))
LIMIT 1;

INSERT INTO staff_role (id, org_id, staff_id, role_id, create_time, update_time, is_deleted)
SELECT UUID_SHORT(), s.org_id, s.id, r.id, NOW(), NOW(), 0
FROM staff s
JOIN role r ON r.org_id = s.org_id AND r.role_code = 'GUARD' AND r.is_deleted = 0
WHERE @demo_org_id IS NOT NULL AND s.org_id = @demo_org_id AND s.username = 'guard_demo' AND s.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM staff_role sr WHERE sr.staff_id = s.id AND sr.role_id = r.id AND sr.is_deleted = 0);
