package com.zhiyangyun.care.medicalcare.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

@Data
public class MedicalCvdRiskAssessmentRequest {
  private Long elderId;
  @NotBlank
  private String elderName;
  @NotNull
  private LocalDate assessmentDate;
  private Long assessorId;
  private String assessorName;
  @NotBlank
  private String riskLevel;
  private String keyRiskFactors;
  private String lifestyleJson;
  private String factorJson;
  private String conclusion;
  private String medicalAdvice;
  private String nursingAdvice;
  private Integer followupDays;
  private Integer needFollowup;
  private Integer generateInspectionPlan;
  private Integer generateFollowupTask;
  private Integer suggestMedicalOrder;
  private String status;
}
