package com.zhiyangyun.care.elder.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminFamilyUserUpsertRequest {
  @NotBlank
  private String realName;
  @NotBlank
  private String phone;
  @NotBlank
  private String idCardNo;
  private Integer status;
}

