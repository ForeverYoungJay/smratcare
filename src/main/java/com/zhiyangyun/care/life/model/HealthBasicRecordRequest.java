package com.zhiyangyun.care.life.model;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class HealthBasicRecordRequest {
  private Long elderId;

  private String elderName;

  @NotNull
  private LocalDate recordDate;

  private BigDecimal heightCm;

  private BigDecimal weightKg;

  private BigDecimal bmi;

  private String bloodPressure;

  private Integer heartRate;

  private String remark;
}
