package com.zhiyangyun.care.health.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class HealthMedicationRegistrationRequest {
  private Long elderId;
  @Size(max = 64, message = "elderName too long")
  private String elderName;
  private Long drugId;

  @NotBlank
  @Size(max = 128, message = "drugName too long")
  private String drugName;

  @NotNull
  private LocalDateTime registerTime;

  @NotNull
  @DecimalMin(value = "0.01")
  private BigDecimal dosageTaken;

  @Size(max = 32, message = "unit too long")
  private String unit;
  @Size(max = 64, message = "nurseName too long")
  private String nurseName;
  @Size(max = 1000, message = "remark too long")
  private String remark;
}
