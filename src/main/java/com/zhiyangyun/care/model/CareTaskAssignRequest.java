package com.zhiyangyun.care.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CareTaskAssignRequest {
  @NotNull
  private Long taskDailyId;

  @NotNull
  private Long staffId;

  private Boolean force;
}
