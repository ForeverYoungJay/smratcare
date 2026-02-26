package com.zhiyangyun.care.medicalcare.model;

import lombok.Data;

@Data
public class MedicalResidentRiskCardResponse {
  private Long elderId;
  private String elderName;

  private String latestTcmPrimary;
  private String latestTcmSecondary;
  private String latestTcmDate;
  private String latestTcmSuggestion;

  private String latestCvdRiskLevel;
  private String latestCvdDate;
  private String latestCvdFactors;
  private String latestCvdConclusion;

  private Long abnormalVital24hCount = 0L;
  private Long abnormalInspectionOpenCount = 0L;
  private Long openIncidentCount = 0L;
  private Long pendingCareTaskCount = 0L;

  private String tcmAssessmentRoute;
  private String cvdAssessmentRoute;
}
