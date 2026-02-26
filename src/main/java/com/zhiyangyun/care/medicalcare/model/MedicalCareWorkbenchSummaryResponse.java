package com.zhiyangyun.care.medicalcare.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class MedicalCareWorkbenchSummaryResponse {
  // Card A
  private Long pendingCareTaskCount = 0L;
  private Long overdueCareTaskCount = 0L;
  private Long pendingMedicalOrderCount = 0L;
  private Long pendingReviewCount = 0L;
  private Long pendingAuditCount = 0L;
  private Long unclosedAbnormalCount = 0L;
  private Long todayInspectionTodoCount = 0L;

  // Card B
  private Long topRiskResidentCount = 0L;
  private Long abnormalVital24hCount = 0L;
  private Long abnormalEvent24hCount = 0L;

  // Card C
  private Long medicalOrderShouldCount = 0L;
  private Long medicalOrderDoneCount = 0L;
  private Long medicalOrderPendingCount = 0L;
  private Long medicalOrderAbnormalCount = 0L;
  private Double orderCheckRate = 0D;

  // Card D
  private Long medicationShouldCount = 0L;
  private Long medicationDoneCount = 0L;
  private Long medicationUndoneCount = 0L;
  private Long medicationLowStockCount = 0L;
  private Long medicationRequestPendingCount = 0L;

  // Card E
  private Long careTaskShouldCount = 0L;
  private Long careTaskDoneCount = 0L;
  private Long careTaskOverdueCount = 0L;
  private Double scanExecuteRate = 0D;

  // Card F
  private Long todayInspectionPendingCount = 0L;
  private Long todayInspectionDoneCount = 0L;
  private Long todayInspectionPlanCount = 0L;
  private Long abnormalInspectionCount = 0L;
  private Long nursingLogPendingCount = 0L;

  // Card G
  private Long handoverPendingCount = 0L;
  private Long handoverDoneCount = 0L;
  private Long handoverRiskCount = 0L;
  private Long handoverTodoCount = 0L;

  // Card H
  private Long incidentOpenCount = 0L;
  private Long incident30dCount = 0L;
  private Double incident30dRate = 0D;
  private Long lowScoreSurveyCount = 0L;
  private Long rectifyInProgressCount = 0L;
  private Long rectifyOverdueCount = 0L;

  // Card I
  private Long aiReportGeneratedCount = 0L;
  private Long aiReportPublishedCount = 0L;

  // legacy summary fields for existing cards
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
