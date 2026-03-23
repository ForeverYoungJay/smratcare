ALTER TABLE staff_profile
  ADD COLUMN social_security_company_apply TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否申请企业购买社保' AFTER social_security_reminder_days,
  ADD COLUMN social_security_need_director_approval TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否需院长审核' AFTER social_security_company_apply,
  ADD COLUMN social_security_workflow_status VARCHAR(32) NULL COMMENT '社保流程状态' AFTER social_security_need_director_approval,
  ADD COLUMN social_security_monthly_amount DECIMAL(10,2) NULL COMMENT '员工每月社保费用' AFTER social_security_workflow_status,
  ADD COLUMN social_security_apply_submitted_at DATETIME NULL COMMENT '社保申请提交时间' AFTER social_security_monthly_amount,
  ADD COLUMN social_security_apply_submitted_by BIGINT NULL COMMENT '社保申请提交人' AFTER social_security_apply_submitted_at,
  ADD COLUMN social_security_director_decision_at DATETIME NULL COMMENT '院长审核时间' AFTER social_security_apply_submitted_by,
  ADD COLUMN social_security_director_decision_by BIGINT NULL COMMENT '院长审核人' AFTER social_security_director_decision_at,
  ADD COLUMN social_security_finance_todo_id BIGINT NULL COMMENT '财务办理待办ID' AFTER social_security_director_decision_by,
  ADD COLUMN social_security_completed_at DATETIME NULL COMMENT '社保办理完成时间' AFTER social_security_finance_todo_id,
  ADD COLUMN social_security_last_billed_month VARCHAR(7) NULL COMMENT '最后已生成记账月份' AFTER social_security_completed_at;

UPDATE staff_profile
SET social_security_company_apply = CASE
    WHEN social_security_status IS NOT NULL AND TRIM(social_security_status) <> '' THEN 1
    ELSE 0
  END,
  social_security_workflow_status = CASE
    WHEN UPPER(COALESCE(social_security_status, '')) IN ('COMPLETED', 'PAID') THEN 'ACTIVE'
    WHEN UPPER(COALESCE(social_security_status, '')) = 'PROCESSING' THEN 'PENDING_FINANCE'
    WHEN UPPER(COALESCE(social_security_status, '')) = 'STOPPED' THEN 'STOPPED'
    ELSE 'DRAFT'
  END;
