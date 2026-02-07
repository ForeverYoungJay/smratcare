package com.zhiyangyun.care.auth.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleRequest {
  private Long orgId;
  @NotBlank
  private String roleName;
  @NotBlank
  private String roleCode;
  private String roleDesc;
  private Integer status = 1;
}
