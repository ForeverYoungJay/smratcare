package com.zhiyangyun.care.health.model;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class HealthDataSummaryResponse {
  private Long totalCount;
  private Long abnormalCount;
  private Long normalCount;
  private Double abnormalRate;
  private LocalDateTime latestMeasuredAt;
  private List<HealthDataTypeStatItem> typeStats;
}
