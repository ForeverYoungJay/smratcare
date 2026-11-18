-- 工作总结主管点评：点评后员工不能再修改或删除该总结
ALTER TABLE oa_work_report
  ADD COLUMN reviewer_id BIGINT NULL,
  ADD COLUMN reviewer_name VARCHAR(64) NULL,
  ADD COLUMN review_comment VARCHAR(1000) NULL,
  ADD COLUMN review_score INT NULL COMMENT '1-5 分',
  ADD COLUMN reviewed_at DATETIME NULL;
