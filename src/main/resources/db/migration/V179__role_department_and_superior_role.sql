ALTER TABLE role
  ADD COLUMN department_id BIGINT NULL COMMENT '所属部门' AFTER org_id,
  ADD COLUMN superior_role_id BIGINT NULL COMMENT '上级领导角色' AFTER department_id;
