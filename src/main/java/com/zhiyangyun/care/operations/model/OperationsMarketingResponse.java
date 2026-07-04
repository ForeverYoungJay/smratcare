package com.zhiyangyun.care.operations.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class OperationsMarketingResponse {
  private String generatedAt;
  private String status;
  private Integer conversionIndex;
  private String conversionLevel;
  private List<MarketingMetric> metrics = new ArrayList<>();
  private List<MarketingFunnelStage> funnelStages = new ArrayList<>();
  private List<MarketingRisk> risks = new ArrayList<>();
  private List<MarketingChannel> channels = new ArrayList<>();
  private List<MarketingAction> actions = new ArrayList<>();
  private List<MarketingFlow> flows = new ArrayList<>();

  @Data
  public static class MarketingMetric {
    private String key;
    private String label;
    private String value;
    private String helper;
    private String tone;
    private String routePath;
  }

  @Data
  public static class MarketingFunnelStage {
    private String key;
    private String title;
    private String value;
    private String status;
    private String routePath;
    private List<String> signals = new ArrayList<>();
  }

  @Data
  public static class MarketingRisk {
    private String key;
    private String title;
    private String owner;
    private String level;
    private String routePath;
    private List<String> signals = new ArrayList<>();
    private List<String> controls = new ArrayList<>();
  }

  @Data
  public static class MarketingChannel {
    private String source;
    private String leadCount;
    private String contractRate;
    private String routePath;
  }

  @Data
  public static class MarketingAction {
    private String title;
    private String owner;
    private String priority;
    private String routePath;
    private String description;
  }

  @Data
  public static class MarketingFlow {
    private String key;
    private String title;
    private String status;
    private String routePath;
    private List<String> steps = new ArrayList<>();
  }
}
