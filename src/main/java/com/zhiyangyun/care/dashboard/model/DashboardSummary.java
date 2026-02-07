package com.zhiyangyun.care.dashboard.model;

import lombok.Data;

@Data
public class DashboardSummary {
  private long careTasksToday;
  private long abnormalTasksToday;
  private long inventoryAlerts;
  private long unpaidBills;
}
