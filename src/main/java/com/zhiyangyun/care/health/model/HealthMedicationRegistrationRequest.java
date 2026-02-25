package com.zhiyangyun.care.health.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class HealthMedicationRegistrationRequest {
  private Long elderId;
  private String elderName;
  private Long drugId;

  @NotBlank
  private String drugName;

  @NotNull
  private LocalDateTime registerTime;

  @NotNull
  @DecimalMin(value = "0.01")
  private BigDecimal dosageTaken;

  private String unit;
  private String nurseName;
  private String remark;
}
