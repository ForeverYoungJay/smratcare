package com.zhiyangyun.care.auth.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FamilySmsCodeVerifyRequest {
  private Long orgId;

  @NotBlank
  @Size(max = 20)
  private String phone;

  @NotBlank
  @Size(max = 10)
  private String verifyCode;

  @Size(max = 24)
  private String scene;

  private Boolean consume;
}
