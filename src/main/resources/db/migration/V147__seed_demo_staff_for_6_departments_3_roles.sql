-- 为示例机构补齐 6 部门 × 3 角色人员初始化数据
-- 角色体系：员工 / 部长 / 院长(管理层) + ADMIN + SYS_ADMIN

SET @demo_seed_enabled := LOWER('${demo_seed_enabled}');
SET @demo_seed_on := IF(@demo_seed_enabled IN ('1', 'true', 'yes', 'on'), 1, 0);

SET @demo_org_id := (
    IF(@demo_seed_on = 1,
      (
        SELECT o.id
        FROM org o
        WHERE o.is_deleted = 0
          AND (
            o.org_code = 'YY-GF-001'
            OR o.org_name = '弋阳龟峰颐养中心'
          )
        ORDER BY o.id
        LIMIT 1
      ),
      NULL
    )
);

-- 部门兜底（兼容旧示例库）
UPDATE department d
SET d.dept_name = '后勤部',
    d.dept_code = 'LOGI',
    d.update_time = NOW()
WHERE d.org_id = @demo_org_id
  AND d.dept_code = 'GUARD'
  AND d.is_deleted = 0;

INSERT INTO department (id, org_id, parent_id, dept_name, dept_code, sort_no, status, create_time, update_time, is_deleted)
SELECT (SELECT COALESCE(MAX(d0.id), 0) + 1 FROM department d0), @demo_org_id, NULL, '护理部', 'CARE', 1, 1, NOW(), NOW(), 0
FROM DUAL
WHERE @demo_org_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM department d WHERE d.org_id = @demo_org_id AND d.dept_code = 'CARE' AND d.is_deleted = 0
  );

INSERT INTO department (id, org_id, parent_id, dept_name, dept_code, sort_no, status, create_time, update_time, is_deleted)
SELECT (SELECT COALESCE(MAX(d0.id), 0) + 1 FROM department d0), @demo_org_id, NULL, '医务部', 'MED', 2, 1, NOW(), NOW(), 0
FROM DUAL
WHERE @demo_org_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM department d WHERE d.org_id = @demo_org_id AND d.dept_code = 'MED' AND d.is_deleted = 0
  );

INSERT INTO department (id, org_id, parent_id, dept_name, dept_code, sort_no, status, create_time, update_time, is_deleted)
SELECT (SELECT COALESCE(MAX(d0.id), 0) + 1 FROM department d0), @demo_org_id, NULL, '财务部', 'FIN', 3, 1, NOW(), NOW(), 0
FROM DUAL
WHERE @demo_org_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM department d WHERE d.org_id = @demo_org_id AND d.dept_code = 'FIN' AND d.is_deleted = 0
  );

INSERT INTO department (id, org_id, parent_id, dept_name, dept_code, sort_no, status, create_time, update_time, is_deleted)
SELECT (SELECT COALESCE(MAX(d0.id), 0) + 1 FROM department d0), @demo_org_id, NULL, '后勤部', 'LOGI', 4, 1, NOW(), NOW(), 0
FROM DUAL
WHERE @demo_org_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM department d WHERE d.org_id = @demo_org_id AND d.dept_code = 'LOGI' AND d.is_deleted = 0
  );

INSERT INTO department (id, org_id, parent_id, dept_name, dept_code, sort_no, status, create_time, update_time, is_deleted)
SELECT (SELECT COALESCE(MAX(d0.id), 0) + 1 FROM department d0), @demo_org_id, NULL, '行政人事部', 'HR', 5, 1, NOW(), NOW(), 0
FROM DUAL
WHERE @demo_org_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM department d WHERE d.org_id = @demo_org_id AND d.dept_code = 'HR' AND d.is_deleted = 0
  );

INSERT INTO department (id, org_id, parent_id, dept_name, dept_code, sort_no, status, create_time, update_time, is_deleted)
SELECT (SELECT COALESCE(MAX(d0.id), 0) + 1 FROM department d0), @demo_org_id, NULL, '市场部', 'MKT', 6, 1, NOW(), NOW(), 0
FROM DUAL
WHERE @demo_org_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM department d WHERE d.org_id = @demo_org_id AND d.dept_code = 'MKT' AND d.is_deleted = 0
  );

-- 补齐角色（和 V132 兼容，避免旧库缺失）
INSERT INTO role (id, org_id, role_name, role_code, role_desc, status, create_time, update_time, is_deleted)
SELECT UUID_SHORT(), @demo_org_id, '院长/管理层', 'DIRECTOR', '全院经营管理层', 1, NOW(), NOW(), 0
FROM DUAL
WHERE @demo_org_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM role r WHERE r.org_id = @demo_org_id AND r.role_code = 'DIRECTOR' AND r.is_deleted = 0
  );

-- 补齐 admin 的 SYS_ADMIN（若缺失）
INSERT INTO staff_role (id, org_id, staff_id, role_id, create_time, update_time, is_deleted)
SELECT UUID_SHORT(), s.org_id, s.id, r.id, NOW(), NOW(), 0
FROM staff s
JOIN role r ON r.org_id = s.org_id
WHERE s.org_id = @demo_org_id
  AND s.username = 'admin'
  AND r.role_code = 'SYS_ADMIN'
  AND s.is_deleted = 0
  AND r.is_deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM staff_role sr
    WHERE sr.org_id = s.org_id
      AND sr.staff_id = s.id
      AND sr.role_id = r.id
      AND sr.is_deleted = 0
  );

-- 补齐 6 部门 × 3 角色示例人员（admin 保留现有）
INSERT INTO staff (id, org_id, department_id, staff_no, username, password_hash, real_name, phone, email, gender, status, last_login_time, create_time, update_time, is_deleted)
SELECT p.staff_id, @demo_org_id, d.id, p.staff_no, p.username, '$2a$10$A1rIAhc8BXcMXR/imDcBMeabjSM2/2z4pIjpGYBnMJ8Z.C8Z1e/Ym',
       p.real_name, p.phone, NULL, p.gender, 1, NULL, NOW(), NOW(), 0
FROM (
    SELECT 15001 AS staff_id, 'CARE' AS dept_code, 'DEMO-1001' AS staff_no, 'nursing_emp' AS username, '护理员工王' AS real_name, '13000001001' AS phone, 2 AS gender
    UNION ALL SELECT 15002, 'CARE', 'DEMO-1002', 'nursing_minister', '护理部长李', '13000001002', 1
    UNION ALL SELECT 15003, 'CARE', 'DEMO-1003', 'nursing_director', '护理管理层赵', '13000001003', 1
    UNION ALL SELECT 15101, 'MED', 'DEMO-1101', 'medical_emp', '医疗员工钱', '13000001101', 2
    UNION ALL SELECT 15102, 'MED', 'DEMO-1102', 'medical_minister', '医疗部长孙', '13000001102', 1
    UNION ALL SELECT 15103, 'MED', 'DEMO-1103', 'medical_director', '医疗管理层周', '13000001103', 1
    UNION ALL SELECT 15201, 'FIN', 'DEMO-1201', 'finance_emp', '财务员工吴', '13000001201', 2
    UNION ALL SELECT 15202, 'FIN', 'DEMO-1202', 'finance_minister', '财务部长郑', '13000001202', 1
    UNION ALL SELECT 15203, 'FIN', 'DEMO-1203', 'finance_director', '财务管理层王', '13000001203', 1
    UNION ALL SELECT 15301, 'LOGI', 'DEMO-1301', 'logistics_emp', '后勤员工冯', '13000001301', 1
    UNION ALL SELECT 15302, 'LOGI', 'DEMO-1302', 'logistics_minister', '后勤部长陈', '13000001302', 2
    UNION ALL SELECT 15303, 'LOGI', 'DEMO-1303', 'logistics_director', '后勤管理层褚', '13000001303', 1
    UNION ALL SELECT 15401, 'HR', 'DEMO-1401', 'hr_emp', '人事员工卫', '13000001401', 2
    UNION ALL SELECT 15402, 'HR', 'DEMO-1402', 'hr_minister', '人事部长蒋', '13000001402', 1
    UNION ALL SELECT 15403, 'HR', 'DEMO-1403', 'hr_director', '人事管理层沈', '13000001403', 1
    UNION ALL SELECT 15501, 'MKT', 'DEMO-1501', 'marketing_emp', '市场员工韩', '13000001501', 2
    UNION ALL SELECT 15502, 'MKT', 'DEMO-1502', 'marketing_minister', '市场部长杨', '13000001502', 1
    UNION ALL SELECT 15503, 'MKT', 'DEMO-1503', 'marketing_director', '市场管理层朱', '13000001503', 1
) p
JOIN department d ON d.org_id = @demo_org_id AND d.dept_code = p.dept_code AND d.is_deleted = 0
WHERE @demo_org_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM staff sx WHERE sx.id = p.staff_id AND sx.is_deleted = 0
  )
  AND NOT EXISTS (
    SELECT 1 FROM staff s WHERE s.org_id = @demo_org_id AND s.username = p.username AND s.is_deleted = 0
  );

-- admin 部门归属修正为 HR
UPDATE staff s
JOIN department d ON d.org_id = s.org_id AND d.dept_code = 'HR' AND d.is_deleted = 0
SET s.department_id = d.id,
    s.update_time = NOW()
WHERE s.org_id = @demo_org_id
  AND s.username = 'admin'
  AND s.is_deleted = 0
  AND (s.department_id IS NULL OR s.department_id <> d.id);

-- 人员角色绑定
INSERT INTO staff_role (id, org_id, staff_id, role_id, create_time, update_time, is_deleted)
SELECT UUID_SHORT(), s.org_id, s.id, r.id, NOW(), NOW(), 0
FROM (
    SELECT 'nursing_emp' AS username, 'NURSING_EMPLOYEE' AS role_code
    UNION ALL SELECT 'nursing_minister', 'NURSING_MINISTER'
    UNION ALL SELECT 'nursing_director', 'DIRECTOR'
    UNION ALL SELECT 'medical_emp', 'MEDICAL_EMPLOYEE'
    UNION ALL SELECT 'medical_minister', 'MEDICAL_MINISTER'
    UNION ALL SELECT 'medical_director', 'DIRECTOR'
    UNION ALL SELECT 'finance_emp', 'FINANCE_EMPLOYEE'
    UNION ALL SELECT 'finance_minister', 'FINANCE_MINISTER'
    UNION ALL SELECT 'finance_director', 'DIRECTOR'
    UNION ALL SELECT 'logistics_emp', 'LOGISTICS_EMPLOYEE'
    UNION ALL SELECT 'logistics_minister', 'LOGISTICS_MINISTER'
    UNION ALL SELECT 'logistics_director', 'DIRECTOR'
    UNION ALL SELECT 'hr_emp', 'HR_EMPLOYEE'
    UNION ALL SELECT 'hr_minister', 'HR_MINISTER'
    UNION ALL SELECT 'hr_director', 'DIRECTOR'
    UNION ALL SELECT 'marketing_emp', 'MARKETING_EMPLOYEE'
    UNION ALL SELECT 'marketing_minister', 'MARKETING_MINISTER'
    UNION ALL SELECT 'marketing_director', 'DIRECTOR'
    UNION ALL SELECT 'admin', 'ADMIN'
    UNION ALL SELECT 'admin', 'SYS_ADMIN'
) m
JOIN staff s ON s.org_id = @demo_org_id AND s.username = m.username AND s.is_deleted = 0
JOIN role r ON r.org_id = s.org_id AND r.role_code = m.role_code AND r.is_deleted = 0
WHERE @demo_org_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM staff_role sr
    WHERE sr.org_id = s.org_id
      AND sr.staff_id = s.id
      AND sr.role_id = r.id
      AND sr.is_deleted = 0
  );
