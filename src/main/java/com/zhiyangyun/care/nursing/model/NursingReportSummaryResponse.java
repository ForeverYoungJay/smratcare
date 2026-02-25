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
  private List<NursingStaffWorkloadItem> staffWorkloads;
}
