package com.zhiyangyun.care.operations.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class OperationsLogisticsResponse {
  private String generatedAt;
  private String status;
  private Integer logisticsIndex;
  private String logisticsLevel;
  private List<LogisticsMetric> metrics = new ArrayList<>();
  private List<LogisticsRisk> risks = new ArrayList<>();
  private List<LogisticsAction> actions = new ArrayList<>();
  private List<LogisticsFlow> flows = new ArrayList<>();

  @Data
  public static class LogisticsMetric {
    private String key;
    private String label;
    private String value;
    private String helper;
    private String tone;
    private String routePath;
  }

  @Data
  public static class LogisticsRisk {
    private String key;
    private String title;
    private String owner;
    private String level;
    private String routePath;
    private List<String> signals = new ArrayList<>();
    private List<String> controls = new ArrayList<>();
  }

  @Data
  public static class LogisticsAction {
    private String title;
    private String owner;
    private String priority;
    private String routePath;
    private String description;
  }

  @Data
  public static class LogisticsFlow {
    private String key;
    private String title;
    private String status;
    private String routePath;
    private List<String> steps = new ArrayList<>();
  }
}
