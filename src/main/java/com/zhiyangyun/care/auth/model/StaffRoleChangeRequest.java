package com.zhiyangyun.care.auth.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StaffRoleChangeRequest {
  @NotNull
  private Long orgId;
  @NotNull
  private Long staffId;
  @NotNull
  private Long roleId;
}
