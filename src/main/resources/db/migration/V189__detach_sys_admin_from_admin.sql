-- 设计口径修正：
-- admin 是业务管理员（ADMIN），SYS_ADMIN 是平台构建者的隐藏根身份。
-- 因此 admin 不应自动持有 SYS_ADMIN。

UPDATE staff_role sr
JOIN staff s ON s.id = sr.staff_id
JOIN role r ON r.id = sr.role_id
SET sr.is_deleted = 1,
    sr.update_time = NOW()
WHERE sr.is_deleted = 0
  AND s.is_deleted = 0
  AND r.is_deleted = 0
  AND s.username = 'admin'
  AND r.role_code = 'SYS_ADMIN';
