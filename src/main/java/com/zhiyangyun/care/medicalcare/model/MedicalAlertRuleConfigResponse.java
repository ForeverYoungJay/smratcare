package com.zhiyangyun.care.medicalcare.model;

import lombok.Data;

@Data
public class MedicalAlertRuleConfigResponse {
  private Integer medicationHighDosageThreshold;
  private Integer overdueHoursThreshold;
  private Integer abnormalInspectionRequirePhoto;
  private Integer handoverAutoFillConfirmTime;
  private Integer autoCreateNursingLogFromInspection;
  private Integer autoRaiseTaskFromAbnormal;
  private Integer autoCarryResidentContext;
  private String handoverRiskKeywords;
}
