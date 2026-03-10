package com.zhiyangyun.care.auth.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FamilySmsCodeSendRequest {
  private Long orgId;

  @NotBlank
  @Size(max = 20)
  private String phone;

  @Size(max = 24)
  private String scene;
}
