package com.zhiyangyun.care.auth.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FamilyLoginRequest {
  @NotNull
  private Long orgId;
  @NotBlank
  private String phone;
  @NotBlank
  private String verifyCode;
}
