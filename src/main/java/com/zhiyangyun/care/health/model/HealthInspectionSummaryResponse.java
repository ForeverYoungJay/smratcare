package com.zhiyangyun.care.health.model;

import java.util.List;
import lombok.Data;

@Data
public class HealthInspectionSummaryResponse {
  private Long totalCount;
  private Long abnormalCount;
  private Long followingCount;
  private Long closedCount;
  private Long linkedLogCount;
  private List<HealthNameCountStatItem> statusStats;
}
