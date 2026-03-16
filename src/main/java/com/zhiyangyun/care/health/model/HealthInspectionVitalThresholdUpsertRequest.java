package com.zhiyangyun.care.health.model;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class HealthInspectionVitalThresholdUpsertRequest {
  @NotBlank
  private String type;

  private String metricCode;

  private BigDecimal minValue;

  private BigDecimal maxValue;

  private Integer status;

  private String remark;
}
