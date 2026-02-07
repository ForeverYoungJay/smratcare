package com.zhiyangyun.care.auth.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class StaffRoleAssignRequest {
  private Long orgId;
  @NotNull
  private Long staffId;
  @NotEmpty
  private List<Long> roleIds;
}
