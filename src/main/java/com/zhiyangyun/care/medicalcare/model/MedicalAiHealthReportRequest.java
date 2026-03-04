package com.zhiyangyun.care.medicalcare.model;

import java.time.LocalDate;
import lombok.Data;

@Data
public class MedicalAiHealthReportRequest {
  private String type;
  private LocalDate dateFrom;
  private LocalDate dateTo;
}
