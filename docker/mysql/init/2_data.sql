SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS=0;

-- 最小初始化数据（非演示业务数据）
-- 机构：弋阳龟峰颐养中心

INSERT INTO org (
  id, org_code, org_name, org_type, status,
  contact_name, contact_phone, address,
  create_time, update_time, is_deleted
) VALUES (
  1, 'YY-GF-001', '弋阳龟峰颐养中心', '养老机构', 1,
  '管理员', '13000000000', '江西省上饶市弋阳县',
  '2026-03-10 00:00:00', '2026-03-10 00:00:00', 0
);

INSERT INTO department (
  id, org_id, parent_id, dept_name, dept_code,
  sort_no, status, create_time, update_time, is_deleted
) VALUES
  (10, 1, NULL, '护理部', 'CARE', 1, 1, '2026-03-10 00:00:00', '2026-03-10 00:00:00', 0),
  (11, 1, NULL, '医务部', 'MED', 2, 1, '2026-03-10 00:00:00', '2026-03-10 00:00:00', 0),
  (12, 1, NULL, '财务部', 'FIN', 3, 1, '2026-03-10 00:00:00', '2026-03-10 00:00:00', 0),
  (13, 1, NULL, '后勤部', 'LOGI', 4, 1, '2026-03-10 00:00:00', '2026-03-10 00:00:00', 0),
  (14, 1, NULL, '行政人事部', 'HR', 5, 1, '2026-03-10 00:00:00', '2026-03-10 00:00:00', 0),
  (15, 1, NULL, '市场部', 'MKT', 6, 1, '2026-03-10 00:00:00', '2026-03-10 00:00:00', 0);

-- 管理账号（密码：123456）
INSERT INTO role (
  id, org_id, role_name, role_code, role_desc,
  status, create_time, update_time, is_deleted
) VALUES
  (100, 1, '管理员', 'ADMIN', '系统管理员', 1, '2026-03-10 00:00:00', '2026-03-10 00:00:00', 0),
  (101, 1, '系统超管', 'SYS_ADMIN', '系统级超级管理员', 1, '2026-03-10 00:00:00', '2026-03-10 00:00:00', 0);

INSERT INTO staff (
  id, org_id, department_id, staff_no, username, password_hash,
  real_name, phone, email, gender, status, last_login_time,
  create_time, update_time, is_deleted
) VALUES (
  500, 1, 14, 'S0001', 'admin', '$2a$10$A1rIAhc8BXcMXR/imDcBMeabjSM2/2z4pIjpGYBnMJ8Z.C8Z1e/Ym',
  '系统管理员', '13000000000', NULL, 1, 1, NULL,
  '2026-03-10 00:00:00', '2026-03-10 00:00:00', 0
);

INSERT INTO staff_role (
  id, org_id, staff_id, role_id, create_time, update_time, is_deleted
) VALUES
  (600, 1, 500, 100, '2026-03-10 00:00:00', '2026-03-10 00:00:00', 0),
  (601, 1, 500, 101, '2026-03-10 00:00:00', '2026-03-10 00:00:00', 0);

SET FOREIGN_KEY_CHECKS=1;
