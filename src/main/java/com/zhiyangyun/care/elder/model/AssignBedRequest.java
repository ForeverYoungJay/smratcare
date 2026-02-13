package com.zhiyangyun.care.elder.model;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

@Data
public class AssignBedRequest {
  private Long tenantId;
  private Long createdBy;
  @NotNull
  private Long bedId;
  @NotNull
  private LocalDate startDate;
}
