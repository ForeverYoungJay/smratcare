package com.zhiyangyun.care.standard.model;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

@Data
public class ElderPackageRequest {
  private Long tenantId;
  private Long orgId;
  @NotNull
  private Long elderId;
  @NotNull
  private Long packageId;
  @NotNull
  private LocalDate startDate;
  private LocalDate endDate;
  private Integer status = 1;
  private String remark;
  private Long createdBy;
}
