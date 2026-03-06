INSERT INTO role (id, org_id, role_name, role_code, role_desc, status, create_time, update_time, is_deleted)
SELECT UUID_SHORT(), o.id, '系统超管', 'SYS_ADMIN', '系统级超级管理员', 1, NOW(), NOW(), 0
FROM org o
WHERE o.is_deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM role r
    WHERE r.org_id = o.id
      AND r.role_code = 'SYS_ADMIN'
      AND r.is_deleted = 0
  );

INSERT INTO role (id, org_id, role_name, role_code, role_desc, status, create_time, update_time, is_deleted)
SELECT UUID_SHORT(), o.id, '医疗部员工', 'MEDICAL_EMPLOYEE', '医疗部执行岗', 1, NOW(), NOW(), 0
FROM org o
WHERE o.is_deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM role r
    WHERE r.org_id = o.id
      AND r.role_code = 'MEDICAL_EMPLOYEE'
      AND r.is_deleted = 0
  );

INSERT INTO role (id, org_id, role_name, role_code, role_desc, status, create_time, update_time, is_deleted)
SELECT UUID_SHORT(), o.id, '医疗部部长', 'MEDICAL_MINISTER', '医疗部管理岗', 1, NOW(), NOW(), 0
FROM org o
WHERE o.is_deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM role r
    WHERE r.org_id = o.id
      AND r.role_code = 'MEDICAL_MINISTER'
      AND r.is_deleted = 0
  );

INSERT INTO role (id, org_id, role_name, role_code, role_desc, status, create_time, update_time, is_deleted)
SELECT UUID_SHORT(), o.id, '护理部员工', 'NURSING_EMPLOYEE', '护理部执行岗', 1, NOW(), NOW(), 0
FROM org o
WHERE o.is_deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM role r
    WHERE r.org_id = o.id
      AND r.role_code = 'NURSING_EMPLOYEE'
      AND r.is_deleted = 0
  );

INSERT INTO role (id, org_id, role_name, role_code, role_desc, status, create_time, update_time, is_deleted)
SELECT UUID_SHORT(), o.id, '护理部部长', 'NURSING_MINISTER', '护理部管理岗', 1, NOW(), NOW(), 0
FROM org o
WHERE o.is_deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM role r
    WHERE r.org_id = o.id
      AND r.role_code = 'NURSING_MINISTER'
      AND r.is_deleted = 0
  );

INSERT INTO role (id, org_id, role_name, role_code, role_desc, status, create_time, update_time, is_deleted)
SELECT UUID_SHORT(), o.id, '财务部员工', 'FINANCE_EMPLOYEE', '财务部执行岗', 1, NOW(), NOW(), 0
FROM org o
WHERE o.is_deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM role r
    WHERE r.org_id = o.id
      AND r.role_code = 'FINANCE_EMPLOYEE'
      AND r.is_deleted = 0
  );

INSERT INTO role (id, org_id, role_name, role_code, role_desc, status, create_time, update_time, is_deleted)
SELECT UUID_SHORT(), o.id, '财务部部长', 'FINANCE_MINISTER', '财务部管理岗', 1, NOW(), NOW(), 0
FROM org o
WHERE o.is_deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM role r
    WHERE r.org_id = o.id
      AND r.role_code = 'FINANCE_MINISTER'
      AND r.is_deleted = 0
  );

INSERT INTO role (id, org_id, role_name, role_code, role_desc, status, create_time, update_time, is_deleted)
SELECT UUID_SHORT(), o.id, '后勤部员工', 'LOGISTICS_EMPLOYEE', '后勤部执行岗', 1, NOW(), NOW(), 0
FROM org o
WHERE o.is_deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM role r
    WHERE r.org_id = o.id
      AND r.role_code = 'LOGISTICS_EMPLOYEE'
      AND r.is_deleted = 0
  );

INSERT INTO role (id, org_id, role_name, role_code, role_desc, status, create_time, update_time, is_deleted)
SELECT UUID_SHORT(), o.id, '后勤部部长', 'LOGISTICS_MINISTER', '后勤部管理岗', 1, NOW(), NOW(), 0
FROM org o
WHERE o.is_deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM role r
    WHERE r.org_id = o.id
      AND r.role_code = 'LOGISTICS_MINISTER'
      AND r.is_deleted = 0
  );

INSERT INTO role (id, org_id, role_name, role_code, role_desc, status, create_time, update_time, is_deleted)
SELECT UUID_SHORT(), o.id, '市场部员工', 'MARKETING_EMPLOYEE', '市场部执行岗', 1, NOW(), NOW(), 0
FROM org o
WHERE o.is_deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM role r
    WHERE r.org_id = o.id
      AND r.role_code = 'MARKETING_EMPLOYEE'
      AND r.is_deleted = 0
  );

INSERT INTO role (id, org_id, role_name, role_code, role_desc, status, create_time, update_time, is_deleted)
SELECT UUID_SHORT(), o.id, '市场部部长', 'MARKETING_MINISTER', '市场部管理岗', 1, NOW(), NOW(), 0
FROM org o
WHERE o.is_deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM role r
    WHERE r.org_id = o.id
      AND r.role_code = 'MARKETING_MINISTER'
      AND r.is_deleted = 0
  );

INSERT INTO role (id, org_id, role_name, role_code, role_desc, status, create_time, update_time, is_deleted)
SELECT UUID_SHORT(), o.id, '行政人事部员工', 'HR_EMPLOYEE', '行政人事部执行岗', 1, NOW(), NOW(), 0
FROM org o
WHERE o.is_deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM role r
    WHERE r.org_id = o.id
      AND r.role_code = 'HR_EMPLOYEE'
      AND r.is_deleted = 0
  );

INSERT INTO role (id, org_id, role_name, role_code, role_desc, status, create_time, update_time, is_deleted)
SELECT UUID_SHORT(), o.id, '行政人事部部长', 'HR_MINISTER', '行政人事部管理岗', 1, NOW(), NOW(), 0
FROM org o
WHERE o.is_deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM role r
    WHERE r.org_id = o.id
      AND r.role_code = 'HR_MINISTER'
      AND r.is_deleted = 0
  );

INSERT INTO role (id, org_id, role_name, role_code, role_desc, status, create_time, update_time, is_deleted)
SELECT UUID_SHORT(), o.id, '院长/管理层', 'DIRECTOR', '全院经营管理层', 1, NOW(), NOW(), 0
FROM org o
WHERE o.is_deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM role r
    WHERE r.org_id = o.id
      AND r.role_code = 'DIRECTOR'
      AND r.is_deleted = 0
  );

INSERT INTO staff_role (id, org_id, staff_id, role_id, create_time, update_time, is_deleted)
SELECT UUID_SHORT(), admin_sr.org_id, admin_sr.staff_id, sys_role.id, NOW(), NOW(), 0
FROM staff_role admin_sr
JOIN role admin_role ON admin_role.id = admin_sr.role_id
  AND admin_role.role_code = 'ADMIN'
  AND admin_role.is_deleted = 0
JOIN role sys_role ON sys_role.org_id = admin_sr.org_id
  AND sys_role.role_code = 'SYS_ADMIN'
  AND sys_role.is_deleted = 0
WHERE admin_sr.is_deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM staff_role target
    WHERE target.org_id = admin_sr.org_id
      AND target.staff_id = admin_sr.staff_id
      AND target.role_id = sys_role.id
      AND target.is_deleted = 0
  );
