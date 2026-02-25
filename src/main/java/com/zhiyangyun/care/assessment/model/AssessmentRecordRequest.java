package com.zhiyangyun.care.assessment.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class AssessmentRecordRequest {
  private Long elderId;

  @NotBlank
  private String elderName;

  @NotBlank
  private String assessmentType;

  private Long templateId;

  private String levelCode;

  private BigDecimal score;

  @NotNull
  private LocalDate assessmentDate;

  private LocalDate nextAssessmentDate;

  private Long assessorId;

  private String assessorName;

  private String status;

  private String resultSummary;

  private String suggestion;

  private String detailJson;

  private Integer scoreAuto;

  private String archiveNo;

  private String source;
}
