package com.zhiyangyun.care.schedule.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShiftSwapRequestCreateRequest {
  @NotNull
  private Long applicantScheduleId;
  @NotNull
  private Long targetScheduleId;
  private String applicantRemark;
}
