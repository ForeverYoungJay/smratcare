package com.zhiyangyun.care.health.model;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class HealthMedicationRegistrationSummaryResponse {
  private Long totalCount;
  private Long todayCount;
  private BigDecimal totalDosage;
  private Long doneTaskCount;
  private Long pendingTaskCount;
  private List<HealthNameCountStatItem> nurseStats;
}
