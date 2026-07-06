package com.zhiyangyun.care.auth.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 2FA 第二步：凭挑战令牌 + 短信验证码换取正式登录令牌。
 */
@Data
public class TwoFactorVerifyRequest {
  @NotBlank(message = "挑战令牌不能为空")
  private String challengeToken;
  @NotBlank(message = "验证码不能为空")
  private String verifyCode;
}
