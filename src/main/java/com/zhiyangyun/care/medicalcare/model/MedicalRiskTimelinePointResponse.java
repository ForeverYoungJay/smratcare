package com.zhiyangyun.care.medicalcare.model;

import java.time.LocalDate;
import lombok.Data;

@Data
public class MedicalRiskTimelinePointResponse {
  private LocalDate date;
  private Integer riskScore = 0;
  private String riskLevel = "LOW";
  private Long medicationPendingCount = 0L;
  private Long inspectionOpenCount = 0L;
  private Long careOverdueCount = 0L;
  private Long incidentCount = 0L;
  private Long rectifyOverdueCount = 0L;
  private Integer overdueHours = 12;
}
