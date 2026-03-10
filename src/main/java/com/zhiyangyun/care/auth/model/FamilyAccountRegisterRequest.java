package com.zhiyangyun.care.auth.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FamilyAccountRegisterRequest {
  private Long orgId;

  @NotBlank
  @Size(max = 20)
  private String phone;

  @NotBlank
  @Size(max = 10)
  private String verifyCode;

  @NotBlank
  @Size(min = 6, max = 32)
  private String password;

  @Size(max = 60)
  private String realName;
}
