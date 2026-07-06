package com.zhiyangyun.care.compliance.model;

import java.util.List;
import lombok.Data;

/**
 * 机构安全策略的生效视图（不存在配置行时返回默认值）。
 */
@Data
public class SecurityPolicyView {
  private Long orgId;
  /** 管理端双因子登录开关 */
  private Boolean twoFactorEnabled;
  /** 需要 2FA 的角色（空 = 开启时对全部角色生效） */
  private List<String> twoFactorRoles;
  /** 密码有效期天数（0 = 不启用） */
  private Integer passwordMaxDays;
  /** 连续登录失败锁定次数（0 = 不启用） */
  private Integer loginFailLockCount;
  /** 锁定时长（分钟） */
  private Integer loginFailLockMinutes;
  /** 会话超时分钟（0 = 使用全局 JWT 默认） */
  private Integer sessionTimeoutMinutes;
  /** 敏感字段脱敏展示开关 */
  private Boolean maskingEnabled;
  /** 脱敏豁免角色（可看全文） */
  private List<String> maskingExemptRoles;
  /** 日志留存天数（等保 2.0 下限 180，清理任务硬编码兜底） */
  private Integer logRetentionDays;
}
