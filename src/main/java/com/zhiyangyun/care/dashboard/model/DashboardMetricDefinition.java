package com.zhiyangyun.care.dashboard.model;

import java.util.List;
import lombok.Data;

@Data
public class DashboardMetricDefinition {
  private String metricKey;
  private String metricName;
  private String metricGroup;
  private String formula;
  private String source;
  private String refreshPolicy;
  private String suggestedRoute;
  private List<String> requiredRoles;
}
