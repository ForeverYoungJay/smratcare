package com.zhiyangyun.care.nursing.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Data;

@Data
public class ServicePlanRequest {
  private Long tenantId;
  private Long orgId;
  @NotNull
  private Long elderId;
  private Long careLevelId;
  @NotNull
  private Long serviceItemId;
  @NotBlank
  private String planName;
  private String cycleType = "DAILY";
  private Integer frequency = 1;
  @NotNull
  private LocalDate startDate;
  private LocalDate endDate;
  private LocalTime preferredTime;
  private Long defaultStaffId;
  private String status = "ACTIVE";
  private String remark;
  private Long createdBy;
}
