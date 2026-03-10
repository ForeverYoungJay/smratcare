-- 统一示例员工工号风格：DEMO-xxxx -> Sxxxx
-- 仅处理未删除员工，且避免与现有 staff_no 冲突

SET @demo_seed_enabled := LOWER('${demo_seed_enabled}');
SET @demo_seed_on := IF(@demo_seed_enabled IN ('1', 'true', 'yes', 'on'), 1, 0);

UPDATE staff s
LEFT JOIN staff conflict
  ON conflict.org_id = s.org_id
 AND conflict.staff_no = CONCAT('S', SUBSTRING(s.staff_no, 6, 4))
 AND conflict.id <> s.id
 AND conflict.is_deleted = 0
SET s.staff_no = CONCAT('S', SUBSTRING(s.staff_no, 6, 4)),
    s.update_time = NOW()
WHERE s.is_deleted = 0
  AND @demo_seed_on = 1
  AND s.staff_no REGEXP '^DEMO-[0-9]{4}$'
  AND conflict.id IS NULL;
