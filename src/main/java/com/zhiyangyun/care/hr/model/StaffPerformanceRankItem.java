package com.zhiyangyun.care.hr.model;

import lombok.Data;

@Data
public class StaffPerformanceRankItem {
  private Integer rankNo;
  private Long staffId;
  private String staffName;
  private Integer taskCount;
  private Integer successCount;
  private Double avgScore;
  private Integer periodPoints;
  private Integer pointsBalance;
}
