package com.zhiyangyun.care.operations.model;

import java.util.List;
import lombok.Data;

@Data
public class OperationsCapabilityMapResponse {
  private String version;
  private String generatedAt;
  private String overallStatus;
  private List<CapabilitySummary> summary;
  private List<DomainCapability> domains;
  private List<MetricCapability> metrics;
  private List<IntelligenceCapability> intelligence;

  @Data
  public static class CapabilitySummary {
    private String label;
    private int total;
    private int ready;
    private int partial;
    private int planned;
  }

  @Data
  public static class DomainCapability {
    private String key;
    private String title;
    private String description;
    private String status;
    private String statusText;
    private String routePath;
    private List<String> completed;
    private List<String> nextActions;
    private List<String> evidenceRoutes;
  }

  @Data
  public static class MetricCapability {
    private String key;
    private String name;
    private String routePath;
    private String source;
    private String decisionValue;
  }

  @Data
  public static class IntelligenceCapability {
    private String key;
    private String title;
    private String status;
    private String routePath;
    private List<String> landingSteps;
  }
}
