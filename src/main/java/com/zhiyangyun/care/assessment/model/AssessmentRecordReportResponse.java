package com.zhiyangyun.care.assessment.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AssessmentRecordReportResponse {
  private Long recordId;
  private String reportNo;
  private String reportStatus;
  private String elderName;
  private String assessmentType;
  private String assessmentTypeLabel;
  private String assessorName;
  private LocalDate assessmentDate;
  private LocalDateTime completedTime;
  private BigDecimal score;
  private String levelCode;
  private String resultSummary;
  private String suggestion;
  private String detailJson;
}
