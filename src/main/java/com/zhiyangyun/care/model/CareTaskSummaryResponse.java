package com.zhiyangyun.care.model;

import lombok.Data;

@Data
public class CareTaskSummaryResponse {
  private Long totalCount;
  private Long pendingCount;
  private Long doneCount;
  private Long exceptionCount;
  private Long overdueCount;
  private Long suspiciousCount;
  private Long assignedCount;
  private Long unassignedCount;
  private Double completionRate;
  private Double exceptionRate;
}
