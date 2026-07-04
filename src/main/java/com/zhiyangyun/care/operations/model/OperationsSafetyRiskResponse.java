package com.zhiyangyun.care.operations.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class OperationsSafetyRiskResponse {
  private String generatedAt;
  private String status;
  private String riskLevel;
  private Integer riskIndex;
  private List<SafetyMetric> metrics = new ArrayList<>();
  private List<SafetyRiskSource> riskSources = new ArrayList<>();
  private List<EmergencyFlow> emergencyFlows = new ArrayList<>();
  private List<SafetyAction> actions = new ArrayList<>();
  private List<SafetyRecentEvent> recentEvents = new ArrayList<>();

  @Data
  public static class SafetyMetric {
    private String key;
    private String label;
    private String value;
    private String helper;
    private String tone;
    private String routePath;
  }

  @Data
  public static class SafetyRiskSource {
    private String key;
    private String title;
    private String owner;
    private String status;
    private String routePath;
    private List<String> signals = new ArrayList<>();
    private List<String> controls = new ArrayList<>();
  }

  @Data
  public static class EmergencyFlow {
    private String key;
    private String title;
    private String trigger;
    private String routePath;
    private List<String> steps = new ArrayList<>();
  }

  @Data
  public static class SafetyAction {
    private String title;
    private String priority;
    private String owner;
    private String routePath;
    private String description;
  }

  @Data
  public static class SafetyRecentEvent {
    private Long id;
    private String source;
    private String title;
    private String level;
    private String status;
    private String occurredAt;
    private String routePath;
  }
}
