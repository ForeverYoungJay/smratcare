package com.zhiyangyun.care.life.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class DiningRiskThresholdUpsertRequest {
  @NotBlank
  private String metricCode;

  @NotBlank
  private String metricName;

  @NotNull
  private BigDecimal thresholdValue;

  private Integer enabled;

  private String remark;
}
