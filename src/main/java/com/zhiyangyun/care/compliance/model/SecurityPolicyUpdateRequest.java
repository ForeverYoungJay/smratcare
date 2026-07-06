package com.zhiyangyun.care.compliance.model;

import java.util.List;
import lombok.Data;

/**
 * 安全策略保存请求（机构级）。
 */
@Data
public class SecurityPolicyUpdateRequest {
  private Boolean twoFactorEnabled;
  private List<String> twoFactorRoles;
  private Integer passwordMaxDays;
  private Integer loginFailLockCount;
  private Integer loginFailLockMinutes;
  private Integer sessionTimeoutMinutes;
  private Boolean maskingEnabled;
  private List<String> maskingExemptRoles;
  private Integer logRetentionDays;
}
