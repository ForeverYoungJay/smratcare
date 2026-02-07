package com.zhiyangyun.care.visit.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VisitCheckInRequest {
  @NotNull
  private Long orgId;
  private Long bookingId;
  private String visitCode;
  private String remark;
}
