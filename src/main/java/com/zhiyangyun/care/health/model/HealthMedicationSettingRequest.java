package com.zhiyangyun.care.health.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class HealthMedicationSettingRequest {
  private Long elderId;
  private String elderName;
  private Long drugId;

  @NotBlank
  private String drugName;

  private String dosage;
  @Pattern(regexp = "^(QD|BID|TID|QID|PRN|CUSTOM)?$", message = "invalid frequency")
  private String frequency;
  private String medicationTime;
  private LocalDate startDate;
  private LocalDate endDate;
  @DecimalMin(value = "0.00")
  private BigDecimal minRemainQty;
  private String remark;
}
