package com.zhiyangyun.care.health.model;

import lombok.Data;

@Data
public class HealthArchiveRequest {
  private Long elderId;
  private String elderName;
  private String bloodType;
  private String allergyHistory;
  private String chronicDisease;
  private String medicalHistory;
  private String emergencyContact;
  private String emergencyPhone;
  private String remark;
}
