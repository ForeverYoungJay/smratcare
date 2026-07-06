package com.zhiyangyun.care.auth.model;

import java.util.List;
import lombok.Data;

@Data
public class LoginResponse {
  private String token;
  private StaffInfo staffInfo;
  private List<String> roles;
  private List<String> permissions;
  private List<String> pagePermissions;
  /** 机构安全策略开启 2FA 时为 true：本次未签发正式 token，需调 /api/auth/2fa/verify 完成第二步 */
  private Boolean requireTwoFactor;
  /** 2FA 挑战令牌（5 分钟有效，一次性） */
  private String challengeToken;
  /** 接收短信验证码的手机号（脱敏展示） */
  private String twoFactorPhone;
}
