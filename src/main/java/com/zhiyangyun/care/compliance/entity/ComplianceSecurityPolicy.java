package com.zhiyangyun.care.compliance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 机构级安全策略：2FA / 密码有效期 / 会话超时 / 登录失败锁定 / 脱敏开关 / 日志留存。
 */
@Data
@TableName("compliance_security_policy")
public class ComplianceSecurityPolicy {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Integer twoFactorEnabled;
  private String twoFactorRoles;
  private Integer passwordMaxDays;
  private Integer loginFailLockCount;
  private Integer loginFailLockMinutes;
  private Integer sessionTimeoutMinutes;
  private Integer maskingEnabled;
  private String maskingExemptRoles;
  private Integer logRetentionDays;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
  private Integer isDeleted;
}
