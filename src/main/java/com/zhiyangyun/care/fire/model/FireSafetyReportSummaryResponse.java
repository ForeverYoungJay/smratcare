package com.zhiyangyun.care.fire.model;

import java.util.List;
import lombok.Data;

@Data
public class FireSafetyReportSummaryResponse {
  private Long totalCount;
  private Long openCount;
  private Long closedCount;
  private Long overdueCount;
  private List<FireSafetyTypeCount> typeStats;
}
