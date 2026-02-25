package com.zhiyangyun.care.oa.model;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import lombok.Data;

@Data
public class OaWorkReportRequest {
  @NotBlank
  private String title;

  @NotBlank
  private String reportType;

  private LocalDate reportDate;

  private LocalDate periodStartDate;

  private LocalDate periodEndDate;

  private String contentSummary;

  private String completedWork;

  private String riskIssue;

  private String nextPlan;

  private String status;

  private Long reporterId;

  private String reporterName;
}
