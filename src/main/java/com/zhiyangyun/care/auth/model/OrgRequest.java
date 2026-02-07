package com.zhiyangyun.care.auth.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrgRequest {
  @NotBlank
  private String orgCode;
  @NotBlank
  private String orgName;
  @NotBlank
  private String orgType;
  private Integer status = 1;
  private String contactName;
  private String contactPhone;
  private String address;
}
