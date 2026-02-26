package com.zhiyangyun.care.nursing.model;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

@Data
public class ShiftTemplateApplyRequest {
  @NotNull
  private LocalDate startDate;
  @NotNull
  private LocalDate endDate;
}
