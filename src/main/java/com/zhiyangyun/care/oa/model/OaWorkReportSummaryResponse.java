package com.zhiyangyun.care.oa.model;

import lombok.Data;

@Data
public class OaWorkReportSummaryResponse {
  private long totalCount;
  private long draftCount;
  private long submittedCount;
  private long todaySubmittedCount;
  private long weekSubmittedCount;
  private long monthSubmittedCount;
  private long dayTypeCount;
  private long weekTypeCount;
  private long monthTypeCount;
  private long yearTypeCount;
  private long missingSummaryCount;
}
