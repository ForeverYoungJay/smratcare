package com.zhiyangyun.care.health.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class HealthMedicationTaskActionRequest {
  private LocalDateTime doneTime;
  @DecimalMin(value = "0.01")
  private BigDecimal dosageTaken;
  @Size(max = 32)
  private String unit;
  @Size(max = 64)
  private String nurseName;
  @Size(max = 1000)
  private String remark;
}
