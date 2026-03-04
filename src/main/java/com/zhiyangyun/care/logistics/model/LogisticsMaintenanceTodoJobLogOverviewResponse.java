package com.zhiyangyun.care.logistics.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class LogisticsMaintenanceTodoJobLogOverviewResponse {
  private long recentTotalRuns;
  private long recentSuccessRuns;
  private long recentFailedRuns;
  private Integer recentWindowDays;

  private LocalDateTime lastExecutedAt;
  private String lastStatus;
  private String lastTriggerType;
  private Integer lastDays;
  private Long lastCreatedCount;
  private Long lastSkippedCount;
  private String lastErrorMessage;

  private Long lastFailedLogId;
  private LocalDateTime lastFailedExecutedAt;
  private String lastFailedErrorMessage;
}

