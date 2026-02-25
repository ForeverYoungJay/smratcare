package com.zhiyangyun.care.health.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class HealthMedicationDepositRequest {
  private Long elderId;
  private String elderName;
  private Long drugId;

  @NotBlank
  private String drugName;

  @NotNull
  private LocalDate depositDate;

  @NotNull
  @DecimalMin(value = "0.01")
  private BigDecimal quantity;

  private String unit;
  private LocalDate expireDate;
  private String depositorName;
  private String remark;
}
