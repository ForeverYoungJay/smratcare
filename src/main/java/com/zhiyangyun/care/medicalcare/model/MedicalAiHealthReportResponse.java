package com.zhiyangyun.care.medicalcare.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class MedicalAiHealthReportResponse {
  private Long id;
  private String type;
  private String status;
  private LocalDate dateFrom;
  private LocalDate dateTo;
  private Long highRiskCount;
  private String rangeText;
  private LocalDateTime createdAt;
}
