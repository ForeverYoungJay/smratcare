package com.zhiyangyun.care.oa.model;

import lombok.Data;

@Data
public class OaTaskSummaryResponse {
  private long totalCount;
  private long openCount;
  private long doneCount;
  private long highPriorityCount;
  private long dueTodayCount;
  private long overdueCount;
  private long unassignedCount;
}
