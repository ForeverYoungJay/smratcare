package com.zhiyangyun.care.medicalcare.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class MedicalCareWorkbenchSummaryResponse {
  private Long pendingCareTaskCount = 0L;
  private Long overdueCareTaskCount = 0L;
  private Long todayInspectionPendingCount = 0L;
  private Long todayInspectionDoneCount = 0L;
  private Long abnormalInspectionCount = 0L;
  private Long todayMedicationPendingCount = 0L;
  private Long todayMedicationDoneCount = 0L;
  private Long tcmPublishedCount = 0L;
  private Long cvdHighRiskCount = 0L;
  private Long cvdNeedFollowupCount = 0L;
  private List<ResidentRiskItem> keyResidents = new ArrayList<>();

  @Data
  public static class ResidentRiskItem {
    private Long elderId;
    private String elderName;
    private String riskLevel;
    private String keyRiskFactors;
    private String assessmentDate;
  }
}
