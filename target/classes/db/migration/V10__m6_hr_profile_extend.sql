-- M6 人力资源扩展：离职信息

ALTER TABLE staff_profile
  ADD COLUMN leave_date DATE DEFAULT NULL COMMENT '离职日期',
  ADD COLUMN leave_reason VARCHAR(255) DEFAULT NULL COMMENT '离职原因';
