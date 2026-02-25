package com.zhiyangyun.care.health.model;

import lombok.Data;

@Data
public class HealthDataTypeStatItem {
  private String dataType;
  private Long totalCount;
  private Long abnormalCount;
  private Double abnormalRate;
}
