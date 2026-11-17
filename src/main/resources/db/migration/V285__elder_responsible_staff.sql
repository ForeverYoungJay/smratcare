-- 长者责任护理员：指向 staff.id，可为空；详情页头部与床位页展示。
ALTER TABLE elder ADD COLUMN responsible_staff_id BIGINT DEFAULT NULL COMMENT '责任护理员 staff.id';
