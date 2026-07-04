package com.zhiyangyun.care.nursing.model;

import java.util.List;
import lombok.Data;

@Data
public class NursingReportSummaryResponse {
  private Integer totalServices;
  private Integer completedServices;
  private Double completionRate;
  private Integer overdueCount;
  private Double overdueRate;
  private Integer planBookingCount;
  private Integer planCompletedCount;
  private Double planAchievementRate;
  private Integer qualityReviewCount;
  private Double averageReviewScore;
  private Integer exceptionTaskCount;
  private Integer exceptionResolvedCount;
  private Double exceptionClosureRate;
  private Integer suspiciousExecutionCount;
  private Integer handoverCount;
  private Integer handoverConfirmedCount;
  private Double handoverConfirmRate;
  private String monthlyReportSummary;
  private String aiCareSummary;
  private String familyReadableSummary;
  private List<String> reviewActionItems;
  private List<String> careHighlights;
  private List<String> riskSignals;
  private List<String> nextMonthSuggestions;
  private List<NursingStaffWorkloadItem> staffWorkloads;
}
