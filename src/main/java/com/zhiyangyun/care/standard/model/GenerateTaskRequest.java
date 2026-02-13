package com.zhiyangyun.care.standard.model;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

@Data
public class GenerateTaskRequest {
  private Long tenantId;
  private Long orgId;
  @NotNull
  private LocalDate date;
  private Boolean force = false;
}
