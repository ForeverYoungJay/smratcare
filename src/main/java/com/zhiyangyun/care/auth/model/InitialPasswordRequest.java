package com.zhiyangyun.care.auth.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 默认密码首次登录：凭改密令牌设置新密码，成功后正常登录。 */
@Data
public class InitialPasswordRequest {
  @NotBlank(message = "改密令牌不能为空")
  private String challengeToken;
  @NotBlank(message = "新密码不能为空")
  private String newPassword;
}
