package com.zhiyangyun.care.crm.model.report;

import lombok.Data;

@Data
public class MarketingLeadEntrySummaryResponse {
  private long totalCount;
  private long modeCount;
  private long consultCount;
  private long intentCount;
  private long reservationCount;
  private long invalidCount;
  private long signedContractCount;
  private long unsignedReservationCount;
  private long refundedReservationCount;
  private long callbackDueTodayCount;
  private long callbackOverdueCount;
  private long callbackPendingCount;
  private long missingSourceCount;
  private long missingNextFollowDateCount;
  private long nonStandardSourceCount;
}
