package com.zhiyangyun.care.auth.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 2FA 第二步：重新发送短信验证码。
 */
@Data
public class TwoFactorResendRequest {
  @NotBlank(message = "挑战令牌不能为空")
  private String challengeToken;
}
