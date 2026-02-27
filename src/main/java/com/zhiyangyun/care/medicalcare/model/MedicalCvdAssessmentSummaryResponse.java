package com.zhiyangyun.care.medicalcare.model;

import lombok.Data;

@Data
public class MedicalCvdAssessmentSummaryResponse {
  private long totalCount;
  private long draftCount;
  private long publishedCount;
  private long highRiskCount;
  private long veryHighRiskCount;
  private long needFollowupCount;
  private long followupOverdueCount;
}
