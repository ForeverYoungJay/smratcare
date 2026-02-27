package com.zhiyangyun.care.assessment.model;

import lombok.Data;

@Data
public class AssessmentRecordSummaryResponse {
  private long totalCount;
  private long draftCount;
  private long completedCount;
  private long archivedCount;
  private long reassessOverdueCount;
  private long highRiskCount;
}
