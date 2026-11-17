package com.zhiyangyun.care.nursing.model;

import lombok.Data;

@Data
public class CareIncidentSummaryResponse {
  private String month;
  private long monthTotal;
  private long processingCount;
  private long pendingReviewCount;
  private long closedCount;
  private long openHighCount;
}
