package com.zhiyangyun.care.fire.model;

import java.util.List;
import lombok.Data;

@Data
public class FireSafetyReportSummaryResponse {
  private Long totalCount;
  private Long openCount;
  private Long closedCount;
  private Long overdueCount;
  private Long dailyCompletedCount;
  private Long monthlyCompletedCount;
  private Long dutyRecordCount;
  private Long handoverPunchCount;
  private Long equipmentUpdateCount;
  private Long equipmentAgingDisposalCount;
  private Long expiringSoonCount;
  private Long nextCheckDueSoonCount;
  private List<FireSafetyTypeCount> typeStats;
}
