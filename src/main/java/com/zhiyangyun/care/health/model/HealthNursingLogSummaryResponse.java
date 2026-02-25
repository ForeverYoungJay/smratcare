package com.zhiyangyun.care.health.model;

import java.util.List;
import lombok.Data;

@Data
public class HealthNursingLogSummaryResponse {
  private Long totalCount;
  private Long pendingCount;
  private Long doneCount;
  private Long closedCount;
  private Long linkedInspectionCount;
  private List<HealthNameCountStatItem> logTypeStats;
}
