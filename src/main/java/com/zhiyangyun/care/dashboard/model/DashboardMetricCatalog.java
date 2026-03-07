package com.zhiyangyun.care.dashboard.model;

import java.util.List;
import lombok.Data;

@Data
public class DashboardMetricCatalog {
  private String metricVersion;
  private String effectiveDate;
  private String defaultWindow;
  private List<DashboardMetricDefinition> definitions;
}
