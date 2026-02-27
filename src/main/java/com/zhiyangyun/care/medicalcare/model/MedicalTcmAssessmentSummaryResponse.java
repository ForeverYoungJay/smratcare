package com.zhiyangyun.care.medicalcare.model;

import lombok.Data;

@Data
public class MedicalTcmAssessmentSummaryResponse {
  private long totalCount;
  private long draftCount;
  private long publishedCount;
  private long reassessmentCount;
  private long familyVisibleCount;
  private long nursingTaskSuggestedCount;
  private long balancedConstitutionCount;
  private long biasedConstitutionCount;
  private long lowConfidenceCount;
}
