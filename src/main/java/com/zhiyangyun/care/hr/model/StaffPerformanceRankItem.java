package com.zhiyangyun.care.hr.model;

import lombok.Data;

@Data
public class StaffPerformanceRankItem {
  private Integer rankNo;
  private Integer categoryRankNo;
  private Long staffId;
  private String staffName;
  private String staffCategory;
  private String medalLevel;
  private Integer taskCount;
  private Integer successCount;
  private Double avgScore;
  private Integer periodPoints;
  private Integer pointsBalance;
  private Integer redeemableCash;
}
