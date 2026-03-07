package com.zhiyangyun.care.medicalcare.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MedicalAlertRuleConfigRequest {
  @Min(value = 1, message = "medicationHighDosageThreshold must be >= 1")
  @Max(value = 999, message = "medicationHighDosageThreshold must be <= 999")
  private Integer medicationHighDosageThreshold;

  @Min(value = 1, message = "overdueHoursThreshold must be >= 1")
  @Max(value = 168, message = "overdueHoursThreshold must be <= 168")
  private Integer overdueHoursThreshold;

  private Integer abnormalInspectionRequirePhoto;
  private Integer handoverAutoFillConfirmTime;
  private Integer autoCreateNursingLogFromInspection;
  private Integer autoRaiseTaskFromAbnormal;
  private Integer autoCarryResidentContext;

  @Size(max = 500, message = "handoverRiskKeywords too long")
  private String handoverRiskKeywords;
}
