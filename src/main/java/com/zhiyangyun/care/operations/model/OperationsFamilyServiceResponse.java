package com.zhiyangyun.care.operations.model;

import java.util.List;
import lombok.Data;

@Data
public class OperationsFamilyServiceResponse {
  private String generatedAt;
  private String status;
  private List<FamilyServiceMetric> metrics;
  private List<FamilyRuntimeCard> runtimeCards;
  private List<FamilyServiceFlow> flows;
  private List<FamilyVisibleData> visibleData;
  private List<FamilyServiceAction> actions;

  @Data
  public static class FamilyServiceMetric {
    private String key;
    private String label;
    private String value;
    private String helper;
    private String routePath;
  }

  @Data
  public static class FamilyRuntimeCard {
    private String key;
    private String title;
    private String value;
    private String status;
    private String helper;
    private String routePath;
  }

  @Data
  public static class FamilyServiceFlow {
    private String key;
    private String title;
    private String status;
    private String routePath;
    private String familyApi;
    private String adminRoute;
    private List<String> steps;
  }

  @Data
  public static class FamilyVisibleData {
    private String key;
    private String title;
    private String status;
    private String familyApi;
    private String adminRoute;
    private List<String> fields;
  }

  @Data
  public static class FamilyServiceAction {
    private String title;
    private String owner;
    private String priority;
    private String routePath;
    private String description;
  }
}
