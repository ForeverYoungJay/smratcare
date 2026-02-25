package com.zhiyangyun.care.elder.model.lifecycle;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

@Data
public class DischargeApplyCreateRequest {
  @NotNull
  private Long elderId;
  @NotNull
  private LocalDate plannedDischargeDate;
  @NotBlank
  private String reason;
}
