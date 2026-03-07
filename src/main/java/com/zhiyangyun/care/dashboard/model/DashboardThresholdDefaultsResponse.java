package com.zhiyangyun.care.dashboard.model;

import lombok.Data;

@Data
public class DashboardThresholdDefaultsResponse {
  private long abnormalTaskThreshold;
  private long inventoryAlertThreshold;
  private long bedOccupancyThreshold;
  private long revenueDropThreshold;
  private String configVersion;
}
