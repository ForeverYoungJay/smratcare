package com.zhiyangyun.care.elder.model.lifecycle;

import lombok.Data;

@Data
public class ResidenceStatusSummaryResponse {
  private long intentCount;
  private long trialCount;
  private long inHospitalCount;
  private long outingCount;
  private long medicalOutingCount;
  private long dischargePendingCount;
  private long dischargedCount;
  private long deathCount;

  private long openIncidentCount;
  private long overdueOutingCount;
}
