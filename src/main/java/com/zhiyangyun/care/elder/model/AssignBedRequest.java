package com.zhiyangyun.care.elder.model;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

@Data
public class AssignBedRequest {
  @NotNull
  private Long bedId;
  @NotNull
  private LocalDate startDate;
}
