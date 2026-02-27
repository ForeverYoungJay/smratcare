package com.zhiyangyun.care.crm.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class CrmContractAssessmentReportItem {
  private Long recordId;
  private String assessmentType;
  private LocalDate assessmentDate;
  private String status;
  private BigDecimal score;
  private String levelCode;
  private String resultSummary;
  private LocalDate nextAssessmentDate;
}
