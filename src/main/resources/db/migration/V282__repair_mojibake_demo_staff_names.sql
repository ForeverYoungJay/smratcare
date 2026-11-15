-- 仅修复已知的示例账号乱码值，避免覆盖用户后续编辑的姓名。
UPDATE staff s JOIN org o ON o.id = s.org_id
SET s.real_name = '护理员工王', s.update_time = NOW()
WHERE o.org_code = 'YY-GF-001' AND s.username = 'nursing_emp' AND s.staff_no IN ('DEMO-1001', 'S1001')
  AND HEX(s.real_name) = 'C3A6C5A0C2A4C3A7C290E280A0C3A5E28098CB9CC3A5C2B7C2A5C3A7C5BDE280B9' AND s.is_deleted = 0;

UPDATE staff s JOIN org o ON o.id = s.org_id
SET s.real_name = '医疗员工钱', s.update_time = NOW()
WHERE o.org_code = 'YY-GF-001' AND s.username = 'medical_emp' AND s.staff_no IN ('DEMO-1101', 'S1101')
  AND HEX(s.real_name) = 'C3A5C592C2BBC3A7E28093E28094C3A5E28098CB9CC3A5C2B7C2A5C3A9E28099C2B1' AND s.is_deleted = 0;

UPDATE staff s JOIN org o ON o.id = s.org_id
SET s.real_name = '财务员工吴', s.update_time = NOW()
WHERE o.org_code = 'YY-GF-001' AND s.username = 'finance_emp' AND s.staff_no IN ('DEMO-1201', 'S1201')
  AND HEX(s.real_name) = 'C3A8C2B4C2A2C3A5C5A0C2A1C3A5E28098CB9CC3A5C2B7C2A5C3A5C290C2B4' AND s.is_deleted = 0;

UPDATE staff s JOIN org o ON o.id = s.org_id
SET s.real_name = '人事员工卫', s.update_time = NOW()
WHERE o.org_code = 'YY-GF-001' AND s.username = 'hr_emp' AND s.staff_no IN ('DEMO-1401', 'S1401')
  AND HEX(s.real_name) = 'C3A4C2BAC2BAC3A4C2BAE280B9C3A5E28098CB9CC3A5C2B7C2A5C3A5C28DC2AB' AND s.is_deleted = 0;
