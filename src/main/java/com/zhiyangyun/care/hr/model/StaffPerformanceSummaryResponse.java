package com.zhiyangyun.care.hr.model;

import lombok.Data;

@Data
public class StaffPerformanceSummaryResponse {
  private Long staffId;
  private String staffName;
  private Integer taskCount;
  private Integer successCount;
  private Integer failCount;
  private Integer suspiciousCount;
  private Double avgScore;
  private Integer pointsBalance;
  private Integer pointsEarned;
  private Integer pointsDeducted;
}
