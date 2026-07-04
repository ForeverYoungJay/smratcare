package com.zhiyangyun.care.operations.model;

import java.util.List;
import lombok.Data;

@Data
public class OperationsIntelligenceResponse {
  private String generatedAt;
  private String status;
  private List<IntelligenceMetric> metrics;
  private List<IntelligenceScenario> scenarios;
  private List<IntelligenceDataSource> dataSources;
  private List<IntelligenceAction> actions;

  @Data
  public static class IntelligenceMetric {
    private String key;
    private String label;
    private String value;
    private String helper;
    private String routePath;
  }

  @Data
  public static class IntelligenceScenario {
    private String key;
    private String title;
    private String status;
    private String routePath;
    private String owner;
    private List<String> landed;
    private List<String> nextSteps;
  }

  @Data
  public static class IntelligenceDataSource {
    private String key;
    private String title;
    private String status;
    private String routePath;
    private List<String> signals;
  }

  @Data
  public static class IntelligenceAction {
    private String title;
    private String owner;
    private String priority;
    private String routePath;
    private String description;
  }
}
